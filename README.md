# How this repo works
The main idea of this repository is use the Openshift Pipelines(Tekton) for CI and Openshift GitOps(Argo) for CD.

Install operators:
- RH Openshift GitOps
- RH Openshift Pipelines
- Nexus

There are some projects that we need to this sample:
- hello-dev
- hello-prod
- hello-pipeline
- nexus
- sonarqube
- argocd

### Deploy and create a Nexus instance
[NEXUS](https://github.com/luizfao/demo-pipeline-tkn/blob/main/nexus/README.md)


### Deploy SonarQube
[SONARQUBE](https://github.com/luizfao/demo-pipeline-tkn/blob/main/sonarqube/sonarqube.adoc)


### Initialize variables to be used
```shell
export GITHUB_USER=luizfao
export GITHUB_TOKEN=generate your token
export GITHUB_URL=https://github.com/luizfao/demo-pipeline-tkn
```

### Create the springs projects.
```shell
oc new-project hello-dev
oc new-project hello-prod
oc new-project hello-pipeline
```

We're gonna use the *hello-pipeline* project to build and run the openshift pipeline, so for that we have to give the right permission for the service account *pipeline* then it will be able to execute the necessaries tasks on the other two springs projects.

```shell
    oc policy add-role-to-user \
    edit \
    system:serviceaccount:hello-pipeline:pipeline \
    --rolebinding-name=pipeline-edit \
    -n hello-dev
```
```shell
    oc policy add-role-to-user \
    edit \
    system:serviceaccount:hello-pipeline:pipeline \
    --rolebinding-name=pipeline-edit \
    -n hello-prod
```

Ok, without futher ado, we can finally start the Continuous Integration, so we're gonna create the pipeline as img below:

![Pipeline](https://github.com/luizfao/demo-pipeline-tkn/blob/main/imgs/pipeline.png)

Now let's follow the steps below: 
1- Create a persistent volume for the workspace
```shell
oc apply -f https://raw.githubusercontent.com/luizfao/demo-pipeline-tkn/main/pipelines-src/pvc.yaml -n hello-pipeline
```
2- Create all required tasks
```shell
oc apply -f https://raw.githubusercontent.com/luizfao/demo-pipeline-tkn/main/pipelines-src/task/sonarqube-scanner.yaml -n hello-pipeline
oc apply -f https://raw.githubusercontent.com/luizfao/demo-pipeline-tkn/main/pipelines-src/task/generate-tag.yaml -n hello-pipeline
oc apply -f https://raw.githubusercontent.com/luizfao/demo-pipeline-tkn/main/pipelines-src/task/update-kustomize-repo.yaml -n hello-pipeline
```

3- Create a secret with the github credential
```shell
    oc create secret generic github-personal-access-token \
    --from-literal=username=${GITHUB_USER} \
    --from-literal=password=${GITHUB_TOKEN} \
    --type "kubernetes.io/basic-auth" \
    -n hello-pipeline
```
4- OpenShift pipelines associate credentials with URLs via an annotation on the secret
```shell
    oc annotate secret github-personal-access-token "tekton.dev/git-0=${GITHUB_URL}" -n hello-pipeline
```
5- Attach that secret to pipeline service account
```shell
    oc secrets link pipeline github-personal-access-token -n hello-pipeline
```

6- Let's do the same with nexus credentials
```shell
    oc create secret generic nexus-access \
   --from-literal=username=admin \
   --from-literal=password=admin123 \
   --type "kubernetes.io/basic-auth" \
   -n hello-pipeline

    # Annotate the secret to specify a container registry URL, you can use the service, but the ideia here was to use an external registry
    oc annotate secret nexus-access "tekton.dev/docker-0=nexus-registry-nexus.apps.cluster-h2xgw.h2xgw.example.opentlc.com" -n hello-pipeline

    # Link the secret to the pipeline service account
    oc secrets link pipeline nexus-access -n hello-pipeline
```
7- Make all tasks run as user 1000 (do not allow root)
```shell
oc patch scc pipelines-scc --type='json' -p '[{"op": "replace","path": "/runAsUser/type","value": "MustRunAs"},{"op": "add","path": "/runAsUser/uid","value": 1000}]'
```

8- First build
First jkube execution must be ran locally to create the objects (this pipeline only updates the objects) - checkout this project and go to demo-pipeline-tkn/hello-service folder:
```shell
   mvn oc:resource oc:apply -Djkube.namespace=hello-dev -Djkube.generator.name=hello-service -Djkube.generator.alias=hello-service
```

9- Create the pipeline  
Before creating the pipeline, update the following items:  
9.1- Default cluster name/URL referenced in the pipeline (ie.: sonar, nexus); *do not change nexus URLs*
9.2- Update Sonar Token  
9.3- Create the pipeline:  
```shell
    oc apply -f ./pipelines-src/pipeline/pipeline.yaml -n hello-pipeline
```

10- Install and setup ArgoCD with RH Openshift GitOps Operator  
user: admin / find password in the secret "argocd-cluster"

11- Give permissions to ArgoCD in the hello-prod project:
```shell
    oc policy add-role-to-user \
        edit \
        system:serviceaccount:argocd:argocd-argocd-application-controller \
        --rolebinding-name=argocd-edit \
        -n hello-prod

    # Label the project as ArgoCD managed
    oc label namespace hello-prod argocd.argoproj.io/managed-by=argocd -n hello-prod
```

12- First sync
Update the server name in the deployment-patch image URL in *prod* branch:
https://github.com/luizfao/spring-service-kustomize/blob/prod/spring-service/overlays/production/deployment-patches.yaml

### Triggers - TKN
https://dzone.com/articles/building-a-multi-feature-pipeline-with-openshift-p
