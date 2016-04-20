package resource_types

// This file was generated by the swagger tool.
// Editing this file might prove futile when you re-run the swagger generate command

import (
	"net/http"

	"github.com/go-swagger/go-swagger/httpkit"

	"permissions/models"
)

/*PostResourceTypesIDOK Updated

swagger:response postResourceTypesIdOK
*/
type PostResourceTypesIDOK struct {

	// In: body
	Payload *models.ResourceTypeOut `json:"body,omitempty"`
}

// NewPostResourceTypesIDOK creates PostResourceTypesIDOK with default headers values
func NewPostResourceTypesIDOK() *PostResourceTypesIDOK {
	return &PostResourceTypesIDOK{}
}

// WithPayload adds the payload to the post resource types Id o k response
func (o *PostResourceTypesIDOK) WithPayload(payload *models.ResourceTypeOut) *PostResourceTypesIDOK {
	o.Payload = payload
	return o
}

// SetPayload sets the payload to the post resource types Id o k response
func (o *PostResourceTypesIDOK) SetPayload(payload *models.ResourceTypeOut) {
	o.Payload = payload
}

// WriteResponse to the client
func (o *PostResourceTypesIDOK) WriteResponse(rw http.ResponseWriter, producer httpkit.Producer) {

	rw.WriteHeader(200)
	if o.Payload != nil {
		if err := producer.Produce(rw, o.Payload); err != nil {
			panic(err) // let the recovery middleware deal with this
		}
	}
}

/*PostResourceTypesIDBadRequest Bad Request

swagger:response postResourceTypesIdBadRequest
*/
type PostResourceTypesIDBadRequest struct {

	// In: body
	Payload *models.ErrorOut `json:"body,omitempty"`
}

// NewPostResourceTypesIDBadRequest creates PostResourceTypesIDBadRequest with default headers values
func NewPostResourceTypesIDBadRequest() *PostResourceTypesIDBadRequest {
	return &PostResourceTypesIDBadRequest{}
}

// WithPayload adds the payload to the post resource types Id bad request response
func (o *PostResourceTypesIDBadRequest) WithPayload(payload *models.ErrorOut) *PostResourceTypesIDBadRequest {
	o.Payload = payload
	return o
}

// SetPayload sets the payload to the post resource types Id bad request response
func (o *PostResourceTypesIDBadRequest) SetPayload(payload *models.ErrorOut) {
	o.Payload = payload
}

// WriteResponse to the client
func (o *PostResourceTypesIDBadRequest) WriteResponse(rw http.ResponseWriter, producer httpkit.Producer) {

	rw.WriteHeader(400)
	if o.Payload != nil {
		if err := producer.Produce(rw, o.Payload); err != nil {
			panic(err) // let the recovery middleware deal with this
		}
	}
}

/*PostResourceTypesIDNotFound Not Found

swagger:response postResourceTypesIdNotFound
*/
type PostResourceTypesIDNotFound struct {

	// In: body
	Payload *models.ErrorOut `json:"body,omitempty"`
}

// NewPostResourceTypesIDNotFound creates PostResourceTypesIDNotFound with default headers values
func NewPostResourceTypesIDNotFound() *PostResourceTypesIDNotFound {
	return &PostResourceTypesIDNotFound{}
}

// WithPayload adds the payload to the post resource types Id not found response
func (o *PostResourceTypesIDNotFound) WithPayload(payload *models.ErrorOut) *PostResourceTypesIDNotFound {
	o.Payload = payload
	return o
}

// SetPayload sets the payload to the post resource types Id not found response
func (o *PostResourceTypesIDNotFound) SetPayload(payload *models.ErrorOut) {
	o.Payload = payload
}

// WriteResponse to the client
func (o *PostResourceTypesIDNotFound) WriteResponse(rw http.ResponseWriter, producer httpkit.Producer) {

	rw.WriteHeader(404)
	if o.Payload != nil {
		if err := producer.Produce(rw, o.Payload); err != nil {
			panic(err) // let the recovery middleware deal with this
		}
	}
}

/*PostResourceTypesIDInternalServerError post resource types Id internal server error

swagger:response postResourceTypesIdInternalServerError
*/
type PostResourceTypesIDInternalServerError struct {

	// In: body
	Payload *models.ErrorOut `json:"body,omitempty"`
}

// NewPostResourceTypesIDInternalServerError creates PostResourceTypesIDInternalServerError with default headers values
func NewPostResourceTypesIDInternalServerError() *PostResourceTypesIDInternalServerError {
	return &PostResourceTypesIDInternalServerError{}
}

// WithPayload adds the payload to the post resource types Id internal server error response
func (o *PostResourceTypesIDInternalServerError) WithPayload(payload *models.ErrorOut) *PostResourceTypesIDInternalServerError {
	o.Payload = payload
	return o
}

// SetPayload sets the payload to the post resource types Id internal server error response
func (o *PostResourceTypesIDInternalServerError) SetPayload(payload *models.ErrorOut) {
	o.Payload = payload
}

// WriteResponse to the client
func (o *PostResourceTypesIDInternalServerError) WriteResponse(rw http.ResponseWriter, producer httpkit.Producer) {

	rw.WriteHeader(500)
	if o.Payload != nil {
		if err := producer.Produce(rw, o.Payload); err != nil {
			panic(err) // let the recovery middleware deal with this
		}
	}
}