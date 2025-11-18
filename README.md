# awsLambdaAlarmSend

## Environment variables

Configure the following variables so the Lambda function can request an OAuth token
before calling the target API:

| Name | Description |
| --- | --- |
| `TARGET_API_URL` | URL of the business API that requires the Kiwoom OAuth token. |
| `TARGET_API_APP_KEY` | App key issued by Kiwoom for OAuth authentication. |
| `TARGET_API_SECRET` | Secret key paired with the app key. |
| `KIWOOM_API_BASE_URL` | _(Optional)_ Base URL used for Kiwoom platform APIs. Defaults to `https://api.kiwoom.com`. |

The application first calls the Kiwoom OAuth token endpoint with the provided app key
and secret key. It then uses the returned access token to invoke `TARGET_API_URL` with
the appropriate `Authorization: Bearer <token>` header. All Kiwoom platform URLs are
built from `KIWOOM_API_BASE_URL`, so additional Kiwoom APIs can reuse the same base
domain without repeating it in multiple places.
