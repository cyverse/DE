(ns metadactyl.service.apps.combined
  "This namespace contains an implementation of metadactyl.protocols.Apps that interacts with one
  or more other implementations. This implementation expects at most one the implementations that
  it interacts with to allow users to add new apps and edit existing ones. If this is not the case
  then the first app in the list that is capable of adding or editing apps wins."
  (:use [metadactyl.service.apps.combined.util :as util]
        [metadactyl.util.assertions :only [assert-not-nil]]))

(deftype CombinedApps [clients]
  metadactyl.protocols.Apps

  (listAppCategories [_ params]
    (apply concat (map #(.listAppCategories % params) clients)))

  (hasCategory [_ category-id]
    (some #(.hasCategory % category-id) clients))

  (listAppsInCategory [_ category-id params]
    (assert-not-nil
     [:category-id category-id]
     (when-let [client (first (filter #(.hasCategory % category-id) clients))]
       (.listAppsInCategory client category-id params))))

  (searchApps [_ search-term params]
    (->> (map #(.searchApps % search-term (select-keys params [:search])) clients)
         (remove nil?)
         (combine-app-search-results params)))

  (canEditApps [_]
    (some #(.canEditApps %) clients))

  (addApp [_ app]
    (.addApp (util/get-apps-client clients) app)))
