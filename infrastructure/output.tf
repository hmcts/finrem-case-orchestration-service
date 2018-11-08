output "env" {
  value = "${var.env}"
}

output "idam_s2s_url" {
  value = "${local.idam_s2s_url}"
}

output "test_environment" {
  value = "${local.local_env}"
}

output "vaultUri" {
  value = "${data.azurerm_key_vault.finrem_key_vault.vault_uri}"
}