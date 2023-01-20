package main;

import modicio.api.J;
import modicio.core.*;
import modicio.nativelang.defaults.SimpleDefinitionVerifier;
import modicio.nativelang.defaults.SimpleMapRegistry;
import modicio.nativelang.defaults.SimpleModelVerifier;
import modicio.verification.DefinitionVerifier;
import modicio.verification.ModelVerifier;
import scala.Option;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class MainClass {

    public static void main(String[] args){

        /*
         * SETUP - ONLY ONCE PER APPLICATION CONTEXT
         */

        DefinitionVerifier definitionVerifier = new SimpleDefinitionVerifier();
        ModelVerifier modelVerifier = new SimpleModelVerifier();

        TypeFactory typeFactory = new TypeFactory(definitionVerifier, modelVerifier);
        InstanceFactory instanceFactory = new InstanceFactory(definitionVerifier, modelVerifier);

        Registry registry = new SimpleMapRegistry(typeFactory, instanceFactory);

        typeFactory.setRegistry(registry);
        instanceFactory.setRegistry(registry);

        try {

            CompletableFuture<TypeHandle> root = J.future(typeFactory.newType(
                    ModelElement.ROOT_NAME(),
                    ModelElement.REFERENCE_IDENTITY(),
                    true,
                    J.convert(Optional.of(TimeIdentity.create()))));
            CompletableFuture<Object> typeFuture = J.future(registry.setType(root.get(), false));
            typeFuture.get();

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        /*
         * EXAMPLE USE CASE
         */

        try {

            String myType = "Todo";
            CompletableFuture<TypeHandle> todoType = J.future(typeFactory.newType(myType, ModelElement.REFERENCE_IDENTITY(), false, Option.empty()));
            J.future(registry.setType(todoType.get(), false)).get();

            CompletableFuture<DeepInstance> instanceFuture = J.future(instanceFactory.newInstance("Todo"));

            Optional<DeepInstance> savedInstanceOption = J.convert(J.future(registry.get(instanceFuture.get().instanceId())).get());

            if(savedInstanceOption.isPresent()) {
                DeepInstance myTodo = savedInstanceOption.get();
                System.out.println("Hooray!! id: " + myTodo.getInstanceId());
            }else{
                System.out.println("This did not work...");
            }

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }


    }

}
