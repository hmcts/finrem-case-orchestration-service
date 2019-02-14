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