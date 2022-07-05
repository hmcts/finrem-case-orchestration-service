variable "product" {}

variable "env" {}

variable "common_tags" {
    type = map(string)
}

variable "location" {
    default = "UK South"
}

variable "appinsights_location" {
    default     = "West Europe"
    description = "Location for Application Insights"
}

variable "jenkins_AAD_objectId" {
    description = "(Required) The Azure AD object ID of a user, service principal or security group in the Azure Active Directory tenant for the vault. The object ID must be unique for the list of access policies."
}

variable "component" {
}

variable "tenant_id" {}