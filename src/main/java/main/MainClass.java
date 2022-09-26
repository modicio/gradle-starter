package main;

import modicio.codi.Fragment;
import modicio.codi.api.*;
import modicio.nativelang.defaults.api.SimpleDefinitionVerifierJ;
import modicio.nativelang.defaults.api.SimpleMapRegistryJ;
import modicio.nativelang.defaults.api.SimpleModelVerifierJ;
import modicio.verification.api.DefinitionVerifierJ;
import modicio.verification.api.ModelVerifierJ;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class MainClass {

    public static void main(String[] args){

        /*
         * SETUP - ONLY ONCE PER APPLICATION CONTEXT
         */

        DefinitionVerifierJ definitionVerifierJ = new SimpleDefinitionVerifierJ();
        ModelVerifierJ modelVerifierJ = new SimpleModelVerifierJ();

        TypeFactoryJ typeFactoryJ = new TypeFactoryJ(definitionVerifierJ, modelVerifierJ);
        InstanceFactoryJ instanceFactoryJ = new InstanceFactoryJ(definitionVerifierJ, modelVerifierJ);

        RegistryJ registryJ = new SimpleMapRegistryJ(typeFactoryJ, instanceFactoryJ);

        typeFactoryJ.setRegistryJ(registryJ);
        instanceFactoryJ.setRegistryJ(registryJ);

        /*
         * EXAMPLE USE CASE
         */

        try {

            String myType = "Todo";
            TypeHandleJ  todoType = typeFactoryJ.newTypeJ(myType, Fragment.REFERENCE_IDENTITY(), false);

            CompletableFuture<Object> typeFuture = registryJ.setType(todoType);
            CompletableFuture<DeepInstanceJ> instanceFuture = instanceFactoryJ.newInstanceJ("Todo");

            Optional<DeepInstanceJ> savedInstanceOption = typeFuture.thenCompose(res -> instanceFuture).thenCompose(instance ->
                    registryJ.getJ(instance.instanceId())).get();

            if(savedInstanceOption.isPresent()) {
                DeepInstanceJ myTodo = savedInstanceOption.get();
                System.out.println("Hooray!! id: " + myTodo.getInstanceIdJ());
            }else{
                System.out.println("This did not work...");
            }

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }


    }

}
