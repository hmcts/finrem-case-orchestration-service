variable "reform_service_name" {
  default = "cos"
}

variable "reform_team" {
  default = "finrem"
}

variable "capacity" {
  default = "1"
}

variable "component" {
  type = "string"
}

variable "env" {
  type = "string"
}

variable "product" {
  type = "string"
}

variable "raw_product" {
  default = "finrem"
}

variable "tenant_id" {}

variable "jenkins_AAD_objectId" {
  type        = "string"
  description = "(Required) The Azure AD object ID of a user, service principal or security group in the Azure Active Directory tenant for the vault. The object ID must be unique for the list of access policies."
}

variable "appinsights_instrumentation_key" {
  description = "Instrumentation key of the App Insights instance this webapp should use. Module will create own App Insights resource if this is not provided"
  default = ""
}

variable "finrem_ns_url" {
  default = "http://finrem-ns-aat.service.core-compute-aat.internal"
}

variable "document_generator_baseurl" {
  default = "http://finrem-dgcs-aat.service.core-compute-aat.internal"
}

variable "payment_api_url" {
  default = "http://finrem-ps-aat.service.core-compute-aat.internal"
}

variable "fees_api_url" {
  default = "http://fees-register-api-aat.service.core-compute-aat.internal"
}


variable "idam_api_url" {
  default = "http://betaDevBccidamAppLB.reform.hmcts.net"
}

variable "idam_s2s-auth_url" {
  default = "http://rpe-service-auth-provider-aat.service.core-compute-aat.internal"
}

variable "subscription" {}

variable "location" {
  type    = "string"
  default = "UK South"
}

variable "ilbIp" {}

variable "common_tags" {
  type = "map"
}

variable "swagger_enabled" {
  default = true
}
