# this script:
# * creates a user
# * grants the user asm (ssm)
# * stores credentials in asm

NEW_USER=dynamodb-examples-developer
NEW_USER_CRED_SECRET=dynamodb-examples-developer-creds

# username
user=$(aws iam get-user --user-name $NEW_USER --query 'User.UserName' --output text || echo None)

test "$user" == "None" && user=$(aws iam create-user --user-name $NEW_USER \
  | python -c 'import json,sys;i=json.load(sys.stdin);print(json.dumps(i), file=sys.stderr);print(i["User"]["UserName"])')

# grant user asm/ssm
aws iam attach-user-policy --user-name $user \
  --policy-arn arn:aws:iam::aws:policy/SecretsManagerReadWrite

# clear old credentials
aws iam list-access-keys --user-name $user --query 'AccessKeyMetadata[*].AccessKeyId' --output text \
  | while read old_key ; do
    aws iam delete-access-key --user-name $user --access-key-id $old_key;
  done

# new credentials
credentials="$(aws iam create-access-key --user-name $user --query 'AccessKey' --output json)"
credential_key=$(echo "$credentials" | python -c 'import json,sys;print(json.load(sys.stdin)["AccessKeyId"])')
credential_secret=$(echo "$credentials" | python -c 'import json,sys;print(json.load(sys.stdin)["SecretAccessKey"])')

# store credentials in asm
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

echo ${sec}:
echo "{\"aws_access_key_id\":\"$credential_key\", \"aws_secret_access_key\":\"$credential_secret\"}"
