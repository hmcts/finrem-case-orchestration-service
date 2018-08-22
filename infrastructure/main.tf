locals {
  aseName         = "${data.terraform_remote_state.core_apps_compute.ase_name[0]}"
  local_env       = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "aat" : "saat" : var.env}"
  local_ase       = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "core-compute-aat" : "core-compute-saat" : local.aseName}"
  idam_s2s_url    = "http://rpe-service-auth-provider-${local.local_env}.service.${local.local_ase}.internal"
}

module "finrem-case-progression" {
  source          = "git@github.com:hmcts/moj-module-webapp?ref=master"
  product         = "${var.product}-${var.app}"
  location        = "${var.location}"
  env             = "${var.env}"
  ilbIp           = "${var.ilbIp}"
  subscription    = "${var.subscription}"
  is_frontend     = false
  capacity        = "${var.capacity}"
  common_tags     = "${var.common_tags}"

  app_settings = {
    REFORM_ENVIRONMENT                                    = "${var.env}"
//    AUTH_PROVIDER_SERVICE_CLIENT_BASEURL                  = "${local.idam_s2s_url}"
//    AUTH_PROVIDER_SERVICE_CLIENT_MICROSERVICE             = "${var.auth_provider_service_client_microservice}"
//    AUTH_PROVIDER_SERVICE_CLIENT_KEY                      = "${data.vault_generic_secret.auth-provider-service-client-key.data["value"]}"
//    AUTH_PROVIDER_SERVICE_CLIENT_TOKENTIMETOLIVEINSECONDS = "${var.auth_provider_service_client_tokentimetoliveinseconds}"
//    AUTH_PROVIDER_HEALTH_URI                              = "${local.idam_s2s_url}/health"
  }
}

# region save DB details to Azure Key Vault
module "key-vault" {
  source              = "git@github.com:hmcts/moj-module-key-vault?ref=master"
  name                = "${var.product}-${var.app}-${var.env}"
  product             = "${var.product}"
  env                 = "${var.env}"
  tenant_id           = "${var.tenant_id}"
  object_id           = "${var.jenkins_AAD_objectId}"
  resource_group_name = "${module.finrem-case-progression.resource_group_name}"

  # dcd_cc-dev group object ID
  product_group_object_id = "94ac8962-b614-441b-aa4c-9be878a6bf17"
}

provider "vault" {
  // # tactical vault - for example: use `data "vault_generic_secret" "s2s_secret" {`
  address = "https://vault.reform.hmcts.net:6200"
}

data "vault_generic_secret" "auth-provider-service-client-key" {
  path = "secret/${var.vault_section}/ccidam/service-auth-provider/api/microservice-keys/finrem-case-progression"
}

resource "azurerm_key_vault_secret" "auth-provider-service-client-key" {
  name      = "auth-provider-service-client-key"
  value     = "${data.vault_generic_secret.auth-provider-service-client-key.data["value"]}"
  vault_uri = "${module.key-vault.key_vault_uri}"
}


