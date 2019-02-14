# Temporary fix for template API version error on deployment
provider "azurerm" {
  version = "1.19.0"
}

locals {
  ase_name = "${data.terraform_remote_state.core_apps_compute.ase_name[0]}"
  local_env = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "aat" : "saat" : var.env}"
  previewVaultName = "${var.reform_team}-aat"
  nonPreviewVaultName = "${var.reform_team}-${var.env}"
  vaultName = "${var.env == "preview" ? local.previewVaultName : local.nonPreviewVaultName}"
  vaultUri = "${data.azurerm_key_vault.finrem_key_vault.vault_uri}"

  asp_name = "${var.env == "prod" ? "finrem-cos-prod" : "${var.raw_product}-${var.env}"}"
  asp_rg = "${var.env == "prod" ? "finrem-cos-prod" : "${var.raw_product}-${var.env}"}"
}

module "finrem-cos" {
  source                          = "git@github.com:hmcts/moj-module-webapp.git?ref=master"
  product                         = "${var.product}-${var.component}"
  location                        = "${var.location}"
  env                             = "${var.env}"
  ilbIp                           = "${var.ilbIp}"
  subscription                    = "${var.subscription}"
  appinsights_instrumentation_key = "${var.appinsights_instrumentation_key}"
  capacity                        = "${var.capacity}"
  is_frontend                     = false
  common_tags                     = "${var.common_tags}"
  asp_name                        = "${local.asp_name}"
  asp_rg                          = "${local.asp_rg}"

  app_settings = {
    REFORM_SERVICE_NAME                                   = "${var.reform_service_name}"
    REFORM_TEAM                                           = "${var.reform_team}"
    REFORM_ENVIRONMENT                                    = "${var.env}"
    FINREM_NOTIFICATION_SERVICE_BASE_URL                  = "${var.finrem_ns_url}"
    DOCUMENT_GENERATOR_SERVICE_API_BASEURL                = "${var.document_generator_baseurl}"
    PAYMENT_SERVICE_API_BASEURL                           = "${var.payment_api_url}"
    SWAGGER_ENABLED                                       = "${var.swagger_enabled}"
  }
}

data "azurerm_key_vault" "finrem_key_vault" {
  name                = "${local.vaultName}"
  resource_group_name = "${local.vaultName}"
}

data "azurerm_key_vault_secret" "finrem-case-orchestration-service-s2s-key" {
  name      = "finrem-case-orchestration-service-s2s-key"
  vault_uri = "${data.azurerm_key_vault.finrem_key_vault.vault_uri}"
}