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

variable "client_id" {
  description = "(Required) The object ID of a user, service principal or security group in the Azure Active Directory tenant for the vault. The object ID must be unique for the list of access policies. This is usually sourced from environment variables and not normally required to be specified."
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

variable "idam_api_url" {
  default = "http://betaDevBccidamAppLB.reform.hmcts.net"
}

variable "fees_api_url" {
  default = "http://fees-register-api-aat.service.core-compute-aat.internal"
}

variable "prd_api_url" {
  default = "http://rpa-rd-professional-aat.service.core-compute-aat.internal"
}

variable "idam_s2s_url_prefix" {
  default = "rpe-service-auth-provider"
}

variable "finrem_ns_url_prefix" {
  default = "finrem-ns"
}

variable "auth_provider_service_client_microservice" {
    default = "finrem-case-orchestration"
}

variable "auth_provider_service_client_tokentimetoliveinseconds" {
  default = "900"
}

variable "finrem_case_orchestration_service_api_health_endpoint" {
  default = "/health"
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