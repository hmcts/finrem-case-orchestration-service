output "env" {
  value = "${var.env}"
}

output "vaultName" {
  value = "${module.key-vault.key_vault_name}"
}

output "vaultUri" {
  value = "${module.key-vault.key_vault_uri}"
}

output "idam_s2s_url" {
  value = "${local.idam_s2s_url}"
}

output "test_environment" {
  value = "${local.local_env}"
}
