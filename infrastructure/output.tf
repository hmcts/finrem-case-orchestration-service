output "env" {
  value = "${var.env}"
}

output "finrem_ns_url" {
  value = "${var.finrem_ns_url}"
}

output "document_generator_baseurl" {
  value = "${var.document_generator_baseurl}"
}

output "payment_api_url" {
  value = "${var.payment_api_url}"
}

output "test_environment" {
  value = "${local.local_env}"
}

output "vaultUri" {
  value = "${data.azurerm_key_vault.finrem_key_vault.vault_uri}"
}

output "auth_idam_client_secret" {
  value = "${data.azurerm_key_vault_secret.idam-secret.value}"
}

output "environment_name" {
  value = "${local.local_env}"
}