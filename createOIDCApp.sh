#!/bin/bash

set -eu

if !(type az > /dev/null 2>&1 && type gh > /dev/null 2>&1); then
  echo "should install azure-cli and github-cli."
  exit
fi

appName=functions-deployment-app
repoUser=sasatake
repoName=azure-functions-java-sandbox
branchName=release
description='Deployment Azure Functions Application.'

subscriptionId=$(az account list --all --query '[?isDefault].id' --output tsv --only-show-errors)
tenantId=$(az account list --all --query '[?isDefault].tenantId' --output tsv --only-show-errors)

echo "create app."
appId=$(az ad app create --display-name $appName --query appId --output tsv --only-show-errors)
objectId=$(az ad app list --display-name $appName --query '[0].objectId' --output tsv --only-show-errors)
echo ""

echo "create service principle."
appCount=$(az ad sp list --display-name $appName --query 'length([])' --only-show-errors)
if [ $((appCount)) -eq 0 ]; then
  assigneeObjectId=$(az ad sp create --id $appId --query objectId --output tsv --only-show-errors)
else
  assigneeObjectId=$(az ad sp list --display-name $appName --query '[0].objectId' --output tsv --only-show-errors)
fi
echo ""

echo "create role assignment."
az role assignment create \
  --role "Website Contributor" \
  --description "Assign ${appName} to Website Contributor." \
  --subscription $subscriptionId \
  --assignee-object-id $assigneeObjectId \
  --assignee-principal-type ServicePrincipal \
  --only-show-errors > /dev/null
echo ""
az role assignment create \
  --role "Web Plan Contributor" \
  --description "Assign ${appName} to Website Plan Contributor." \
  --subscription $subscriptionId \
  --assignee-object-id $assigneeObjectId \
  --assignee-principal-type ServicePrincipal \
  --only-show-errors > /dev/null
echo ""

uri="https://graph.microsoft.com/beta/applications/${objectId}/federatedIdentityCredentials"
subject="repo:${repoUser}/${repoName}:ref:refs/heads/${branchName}"

createFederatedIdentity(){
  echo 'create federatedIdentityCredentials.'
  az rest \
    --method POST \
    --uri ${uri} \
    --body "{'name':'${appName}','subject':'${subject}','description':'${description}','issuer':'https://token.actions.githubusercontent.com','audiences':['api://AzureADTokenExchange']}" \
    > /dev/null
}

updateFederatedIdentity(){
  echo 'update federatedIdentityCredentials.'
  az rest \
    --method PATCH \
    --uri "${uri}/${appName}" \
    --body "{'name':'${appName}','subject':'${subject}','description':'${description}','issuer':'https://token.actions.githubusercontent.com','audiences':['api://AzureADTokenExchange']}" \
    > /dev/null
}

federatedIdentityCount=$(az rest --method GET --uri ${uri} --query 'length(value[])')

if [ $((federatedIdentityCount)) -eq 0 ]; then
  createFederatedIdentity
else
  updateFederatedIdentity
fi
echo ""
echo ""

gh secret set AZURE_CLIENT_ID --body ${appId} --app actions --repos ${repoUser}/${repoName}
gh secret set AZURE_SUBSCRIPTION_ID --body ${subscriptionId} --app actions --repos ${repoUser}/${repoName}
gh secret set AZURE_TENANT_ID --body ${tenantId} --app actions --repos ${repoUser}/${repoName}
