# this script:
# * creates a policy with def (if doesn't already exist with same path)
# * creates a user
# * associates user with the policy
# * creates a secret in secrets manager for credentials
#
# it assumes you are logged into the root user of your account with default profile (or similar)
#
# grant yourself sufficient rights if using non-root user
# export your AWS_PROFILE if using a different profile

NEW_USER=dynamodb-examples-dynamodb-user
NEW_POLICY=dynamodb-examples-dynamodb-policy
NEW_POLICY_TABLE_PREFIX='dynamodb-examples-*'
NEW_USER_CRED_SECRET=dynamodb-examples-dynamodb-user-creds

# export AWS_PROFILE
export AWS_DEFAULT_REGION=us-east-1
export AWS_REGION=$AWS_DEFAULT_REGION

POLICY='{"Version": "2012-10-17", "Statement": [{"Effect": "Allow", "Action": "dynamodb:*", "Resource": "arn:aws:dynamodb:*:*:table/'$NEW_POLICY_TABLE_PREFIX'"}]}'

# policy
pol=$(aws iam list-policies --path-prefix /$NEW_POLICY/ --query 'Policies[0].Arn' --output text)
test $pol == "None" && pol=$(
  aws iam create-policy --policy-name $NEW_POLICY --path /$NEW_POLICY/ --policy-document "$POLICY" \
  | python -c 'import json,sys;i=json.load(sys.stdin);print(json.dumps(i), file=sys.stderr);print(i["Policy"]["Arn"])')

# user
user=$(aws iam get-user --user-name $NEW_USER --query 'User.UserName' --output text || echo None)
test $user == "None" && user=$(
  aws iam create-user --user-name $NEW_USER \
  | python -c 'import json,sys;i=json.load(sys.stdin);print(json.dumps(i), file=sys.stderr);print(i["User"]["UserName"])')

# attachment
aws iam attach-user-policy --user-name $user --policy-arn $pol

# clear old credentials
aws iam list-access-keys --user-name $user --query 'AccessKeyMetadata[*].AccessKeyId' --output text \
  | while read old_key ; do
    aws iam delete-access-key --user-name $user --access-key-id $old_key;
  done

# new credentials
credentials="$(aws iam create-access-key --user-name $user --query 'AccessKey' --output json)"
credential_key=$(echo "$credentials" | python -c 'import json,sys;print(json.load(sys.stdin)["AccessKeyId"])')
credential_secret=$(echo "$credentials" | python -c 'import json,sys;print(json.load(sys.stdin)["SecretAccessKey"])')

# secret
#sec=$(aws secretsmanager list-secrets --filters Key=name,Values=$NEW_USER_CRED_SECRET --query 'SecretList[0].ARN' --output text)
#test $sec == "None" &&
old_secret=$(aws secretsmanager list-secrets \
  --filters Key=name,Values=$NEW_USER_CRED_SECRET \
  --query 'SecretList[0].ARN' --output text)

test $old_secret == "None" && {
  sec=$(
    aws secretsmanager create-secret \
      --name $NEW_USER_CRED_SECRET \
      --secret-string \
        "{\"aws_access_key_id\":\"$credential_key\",
        \"aws_secret_access_key\":\"$credential_secret\"}" \
      --query 'ARN' --output text)
} || {
  sec=$old_secret
  aws secretsmanager update-secret \
    --secret-id $sec \
    --secret-string \
      "{\"aws_access_key_id\":\"$credential_key\",
      \"aws_secret_access_key\":\"$credential_secret\"}" \
    --query 'ARN' --output text
}
