# IDAM User Orchestration API

This API automates the IDAM Postman collection steps for test-user management.

It supports:

- Create one or more users
- Delete one or more users
- Update one or more users by deleting each existing account and creating it again

## Base URL

When the service is running locally:

```bash
http://localhost:9000
```

All routes are under:

```text
/case-orchestration/idam/users
```

## Configuration

Create and update need the IDAM `finrem` client secret to get a client-credentials access token.

Preferred configuration:

```bash
export FINREM_IDAM_CLIENT_SECRET=<finrem-client-secret>
```

Optional configuration:

```bash
export IDAM_TEST_SUPPORT_ENV=aat
export IDAM_TEST_SUPPORT_SCOPE="profile roles"
```

Defaults:

| Property | Environment variable | Default |
|----------|----------------------|---------|
| `idam.test-support.default-env` | `IDAM_TEST_SUPPORT_ENV` | `aat` |
| `idam.test-support.client-credentials-scope` | `IDAM_TEST_SUPPORT_SCOPE` | `profile roles` |
| `idam.client.secret` | `FINREM_IDAM_CLIENT_SECRET` | `DUMMY_SECRET` |

If `FINREM_IDAM_CLIENT_SECRET` is not available, create and update can receive a one-off secret using:

```text
X-IDAM-Client-Secret: <finrem-client-secret>
```

Do not put the client secret in the JSON body. This service can log request bodies when local debug body logging is
enabled.

The application cannot fetch the client secret by itself unless it is already available through the configured
environment variable or mounted configtree secret.

## Create User

```http
POST /case-orchestration/idam/users
Content-Type: application/json
```

Request:

```json
{
  "environment": "aat",
  "password": "TempPassword123!",
  "users": [
    {
      "email": "staff.admin@example.com",
      "forename": "Staff",
      "surname": "Admin",
      "roleNames": [
        "staff-admin",
        "caseworker"
      ]
    }
  ]
}
```

`environment` may also be sent as `env`. If omitted, the API uses `IDAM_TEST_SUPPORT_ENV`, defaulting to `aat`.
The same `password` is used for every user in the request.

Curl:

```bash
curl -X POST "http://localhost:9000/case-orchestration/idam/users" \
  -H "Content-Type: application/json" \
  -d '{
    "environment": "aat",
    "password": "TempPassword123!",
    "users": [
      {
        "email": "staff.admin@example.com",
        "forename": "Staff",
        "surname": "Admin",
        "roleNames": ["staff-admin", "caseworker"]
      }
    ]
  }'
```

With one-off client secret override:

```bash
curl -X POST "http://localhost:9000/case-orchestration/idam/users" \
  -H "Content-Type: application/json" \
  -H "X-IDAM-Client-Secret: <finrem-client-secret>" \
  -d '{
    "environment": "aat",
    "password": "TempPassword123!",
    "users": [
      {
        "email": "staff.admin@example.com",
        "forename": "Staff",
        "surname": "Admin",
        "roleNames": ["staff-admin", "caseworker"]
      }
    ]
  }'
```

Successful response status: `201 Created`

## Delete User

```http
DELETE /case-orchestration/idam/users
Content-Type: application/json
```

Request:

```json
{
  "environment": "aat",
  "users": [
    {
      "email": "staff.admin@example.com"
    }
  ]
}
```

Curl:

```bash
curl -X DELETE "http://localhost:9000/case-orchestration/idam/users" \
  -H "Content-Type: application/json" \
  -d '{
    "environment": "aat",
    "users": [
      {
        "email": "staff.admin@example.com"
      }
    ]
  }'
```

Successful response status: `200 OK`

Delete does not need the client secret. It calls the IDAM testing-support account delete endpoint directly.

## Update User

IDAM does not expose an update step in the Postman collection. This route performs:

1. Delete user by `existingEmail`
2. Create user with the supplied replacement user data

```http
PUT /case-orchestration/idam/users
Content-Type: application/json
```

Request:

```json
{
  "environment": "aat",
  "password": "NewTempPassword123!",
  "users": [
    {
      "existingEmail": "staff.admin@example.com",
      "email": "staff.admin@example.com",
      "forename": "Staff",
      "surname": "Admin",
      "roleNames": [
        "staff-admin",
        "caseworker"
      ]
    }
  ]
}
```

The same `password` is used for every recreated user in the request.

Curl:

```bash
curl -X PUT "http://localhost:9000/case-orchestration/idam/users" \
  -H "Content-Type: application/json" \
  -d '{
    "environment": "aat",
    "password": "NewTempPassword123!",
    "users": [
      {
        "existingEmail": "staff.admin@example.com",
        "email": "staff.admin@example.com",
        "forename": "Staff",
        "surname": "Admin",
        "roleNames": ["staff-admin", "caseworker"]
      }
    ]
  }'
```

Successful response status: `200 OK`

## Response Shape

Create response example:

```json
{
  "operation": "CREATE",
  "environment": "aat",
  "users": [
    {
      "id": "00000000-0000-0000-0000-000000000000",
      "email": "staff.admin@example.com",
      "roleNames": [
        "staff-admin",
        "caseworker"
      ]
    }
  ],
  "steps": [
    {
      "name": "GET_CLIENT_CREDENTIALS_TOKEN",
      "target": "idam-web-public",
      "statusCode": 200
    },
    {
      "name": "CREATE_USER",
      "target": "idam-testing-support-api",
      "statusCode": 201
    }
  ]
}
```

Delete response example:

```json
{
  "operation": "DELETE",
  "environment": "aat",
  "users": [
    {
      "email": "staff.admin@example.com",
      "deletedEmail": "staff.admin@example.com"
    }
  ],
  "steps": [
    {
      "name": "DELETE_USER",
      "target": "idam-api",
      "statusCode": 204
    }
  ]
}
```

Update response includes a `users` entry for each recreated user. Each entry includes `deletedEmail` and the recreated
user details.

## Validation

Required fields:

- Create: `password`, `users[].email`, `users[].forename`, `users[].surname`, `users[].roleNames`
- Delete: `users[].email`
- Update: `password`, `users[].existingEmail`, `users[].email`, `users[].forename`, `users[].surname`, `users[].roleNames`

`environment` must contain only letters, numbers and hyphens.

## IDAM Endpoints Called

For environment `aat`, the orchestration calls:

| Step | Method | Target |
|------|--------|--------|
| Get client credentials token | `POST` | `https://idam-web-public.aat.platform.hmcts.net/o/token` |
| Create user | `POST` | `https://idam-testing-support-api.aat.platform.hmcts.net/test/idam/users` |
| Delete user | `DELETE` | `https://idam-api.aat.platform.hmcts.net/testing-support/accounts/{email}` |
