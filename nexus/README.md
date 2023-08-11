# Install Nexus using OperatorHub
Go to Operator Hub install Nexus

## Adjust resource limits (if present)
```shell
oc apply -f https://raw.githubusercontent.com/luizfao/demo-pipeline-tkn/main/nexus/limitRange.yaml -n nexus
```

## After installed the operator apply the following yaml in namespace nexus
```shell
oc apply -f https://raw.githubusercontent.com/luizfao/demo-pipeline-tkn/main/nexus/nexus.yaml -n nexus
```

### Create docker port
```shell
oc patch deployment nexus-registry-sonatype-nexus  -p '{"spec":{"template":{"spec":{"containers":[{"name":"nexus","ports":[{"containerPort": 5000,"protocol":"TCP","name":"docker"}]}]}}}}' -n nexus
```

### Expose Nexus & docker port
```shell
oc expose svc/nexus-registry-sonatype-nexus-service -n nexus
oc expose deployment/nexus-registry-sonatype-nexus --name=nexus-registry --port=5000 -n nexus
```

### Create a edge route to pull the images
```shell
oc create route edge nexus-registry --service=nexus-registry -n nexus
```

### Configure nexus as maven proxy.
https://blog.sonatype.com/using-nexus-3-as-your-repository-part-1-maven-artifacts

### Configure a external registry with Nexus
https://tomd.xyz/openshift-nexus-docker-registry/

### Enable security Realm
Go to Settings > Security > Realms; Add Docker Bearer Token Realm to the Active list.  


