(ns apps.service.apps.de.categorization
  (:use [korma.core :exclude [update]]
        [korma.db :only [transaction]]
        [kameleon.app-groups]
        [kameleon.entities]
        [apps.validation]
        [slingshot.slingshot :only [throw+]])
  (:require [apps.clients.permissions :as perms-client]
            [apps.persistence.app-metadata :as ap]
            [apps.persistence.categories :as db-categories]
            [apps.service.apps.de.validation :as av]
            [apps.util.config :as config]
            [metadata-client.core :as metadata-client]))

(defn get-active-hierarchy-version
  [& {:keys [validate] :or {validate true}}]
  (let [version (db-categories/get-active-hierarchy-version)]
    (when (and validate (empty? version))
      (throw+ {:type  :clojure-commons.exception/not-found
               :error "An app hierarchy version has not been set."}))
    version))

(defn validate-app-name-in-hierarchy-avus
  [username app-id app-name avus]
  (let [category-attrs    (set (config/workspace-metadata-category-attrs))
        hierarchy-avus    (->> avus
                               (filter #(contains? category-attrs (:attr %)))
                               (map #(select-keys % [:attr :value])))
        other-app-ids     (disj (set (keys (perms-client/load-app-permissions username))) app-id)
        hierarchy-app-ids (when-not (or (empty? other-app-ids) (empty? hierarchy-avus))
                            (metadata-client/filter-by-avus username ["app"] other-app-ids hierarchy-avus))]
    (when-not (empty? hierarchy-app-ids)
      (av/validate-app-name-in-hierarchy app-name hierarchy-app-ids))))

(defn validate-app-name-in-current-hierarchy
  [username app-id app-name]
  (validate-app-name-in-hierarchy-avus
    username
    app-id
    app-name
    (-> (metadata-client/list-avus username "app" app-id :as :json)
        :body
        :avus)))

(defn- categorize-app
  "Associates an app with an app category."
  [{app-id :app_id category-ids :category_ids}]
  (decategorize-app app-id)
  (dorun (map (partial add-app-to-category app-id) category-ids)))

(defn- validate-app-info
  "Validates the app information in a categorized app.  At this time, we only
  require the identifier field."
  [app-id path]
  (let [app (get-app-by-id app-id)]
    (when (nil? app)
      (throw+ {:type   :clojure-commons.exception/not-found
               :app-id app-id
               :path   path
               :error  (str "Could not locate app with ID: " app-id)}))))

(defn- load-category
  [category-id]
  (first (select app_categories (with app_categories) (where {:id category-id}))))

(defn- validate-category-id
  [path category-id]
  (let [category (load-category category-id)]
    (when (nil? category)
      (throw+ {:type  :clojure-commons.exception/not-found
               :path  path
               :error (str "Could not locate app category with ID: " category-id)}))

    (when (seq (:app_categories category))
      (throw+ {:type  :clojure-commons.exception/missing-request-field
               :error (str "category " category-id " contains subcategories")
               :path  path}))))

(defn- validate-category-ids
  [category-ids path]
  (when (zero? (count category-ids))
    (throw+ {:type  :clojure-commons.exception/missing-request-field
             :error (str "Missing category ids")
             :path  path}))
  (dorun (map (partial validate-category-id path) category-ids)))

(defn- validate-app-name
  "Validates the app name to ensure that there are no apps with the same name in any of the
  destination categories."
  [app-id category-ids path]
  (av/validate-app-name (ap/get-app-name app-id) app-id category-ids path))

(defn- validate-category
  "Validates each categorized app in the request."
  [{app-id :app_id category-ids :category_ids :as category} path]
  (validate-app-info app-id path)
  (validate-category-ids category-ids path)
  (validate-app-name app-id category-ids path))

(defn- validate-request-body
  "Validates the request body."
  [body]
  (validate-json-object body "" #(validate-json-object-array-field
                                  % :categories %2 validate-category)))

(defn categorize-apps
  "A service that categorizes one or more apps in the database."
  [{:keys [categories] :as body}]
  (transaction
   (validate-request-body body)
   (dorun (map categorize-app categories))))
