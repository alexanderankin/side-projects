import lombok.*;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.BeanTableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static software.amazon.awssdk.services.dynamodb.model.AttributeValue.Type.BOOL;
import static software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType.*;

public class DynamoDbExamples {
    private final DynamoDbClient client;
    private final DynamoDbEnhancedClient enhancedClient;

    public static void main(String[] args) {
        DynamoDbExamples dynamoDbExamples = new DynamoDbExamples();
        dynamoDbExamples.insertData();
        dynamoDbExamples.readData();
    }

    DynamoDbExamples() {
        this.client = DynamoDbClient.builder()
                .credentialsProvider(StaticCredentialsProvider.create(SecretsManagerService.INSTANCE.fetchFromSsm()))
                .build();
        this.enhancedClient = DynamoDbEnhancedClient.builder().dynamoDbClient(client).build();
    }

    private void insertData() {
        // client.waiter()
        //         .waitUntilTableExists(DescribeTableRequest.builder()
        //                 .tableName("dynamodb-examples-donation")
        //                 .build())
        //         .matched().exception().map(lombok.Lombok::sneakyThrow);

        BeanTableSchema<Thing> tableSchema = TableSchema.fromBean(Thing.class);
        DynamoDbTable<Thing> thingTable = enhancedClient.table("dynamodb-examples-thing-test", tableSchema);
        try {
            thingTable.createTable();
            client.waiter().waitUntilTableExists(c -> c.tableName(thingTable.tableName())).matched().exception().map(Lombok::sneakyThrow);
        } catch (ResourceInUseException ignored) {
        }
        thingTable.putItem(Thing.builder().name(UUID.randomUUID().toString()).desc("some desc").build());

        // client.getItem(GetItemRequest.builder().build());

        //
        // client.scan(ScanRequest.builder()
        //                 .filterExpression()
        //                 .se
        //         .build())
        //
        // client.putItem(PutItemRequest.builder()
        //         .item()
        //         .build());
    }

    private void readData() {
        DynamoDbTable<Thing> thingTable = enhancedClient.table("dynamodb-examples-thing-test", TableSchema.fromBean(Thing.class));
        System.out.println(thingTable.scan().items().stream().toList());
    }

    @DynamoDbBean
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Thing {
        String name;
        String desc;
        @Builder.Default
        Instant time = Instant.now();

        @DynamoDbPartitionKey
        public String getName() {
            return name;
        }
    }

    private void createTable() {
        ListGlobalTablesResponse join = client.listGlobalTables();
        System.out.println(join);

        List<CreateTableRequest> tables = new ArrayList<>();
        tables.add(CreateTableRequest.builder()
                .tableName("user")
                .attributeDefinitions(
                        AttributeDefinition.builder().attributeName("id").attributeType(B).build(),
                        AttributeDefinition.builder().attributeName("approved").attributeType(BOOL.name()).build(),
                        AttributeDefinition.builder().attributeName("username").attributeType(S).build(),
                        AttributeDefinition.builder().attributeName("email").attributeType(S).build(),
                        AttributeDefinition.builder().attributeName("password").attributeType(S).build()
                )
                .keySchema(
                        KeySchemaElement.builder().attributeName("id").keyType(KeyType.HASH).build(),
                        KeySchemaElement.builder().attributeName("username").keyType(KeyType.RANGE).build()
                )
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build());
        tables.add(CreateTableRequest.builder()
                .tableName("setting")
                .attributeDefinitions(
                        AttributeDefinition.builder().attributeName("field").attributeType(S).build(),
                        AttributeDefinition.builder().attributeName("value").attributeType(S).build()
                )
                .keySchema(KeySchemaElement.builder().attributeName("field").keyType(KeyType.HASH).build())
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build());
        tables.add(CreateTableRequest.builder()
                .tableName("key")
                .attributeDefinitions(
                        AttributeDefinition.builder().attributeName("code").attributeType(S).build(),
                        AttributeDefinition.builder().attributeName("created_at").attributeType(N).build()
                )
                .keySchema(KeySchemaElement.builder().attributeName("code").keyType(KeyType.HASH).build())
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build());
        tables.add(CreateTableRequest.builder()
                .tableName("donation")
                .attributeDefinitions(
                        AttributeDefinition.builder().attributeName("id").attributeType(B).build(),
                        AttributeDefinition.builder().attributeName("name").attributeType(S).build(),
                        AttributeDefinition.builder().attributeName("when").attributeType(S).build(),
                        AttributeDefinition.builder().attributeName("amount").attributeType(S).build(),
                        AttributeDefinition.builder().attributeName("approved").attributeType(BOOL.name()).build(),
                        AttributeDefinition.builder().attributeName("comment").attributeType(S).build(),
                        AttributeDefinition.builder().attributeName("created_at").attributeType(N).build()
                )
                .keySchema(
                        KeySchemaElement.builder().attributeName("id").keyType(KeyType.HASH).build(),
                        KeySchemaElement.builder().attributeName("when").keyType(KeyType.RANGE).build()
                )
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build());

        tables = tables.stream()
                .map(r -> r.toBuilder()
                        // arn:aws:dynamodb:*:*:table/dynamodb-examples-*
                        // ????
                        .tableName("dynamodb-examples-" + r.tableName())
                        .attributeDefinitions(r.attributeDefinitions().stream()
                                .filter(d -> r.keySchema().stream()
                                        .map(KeySchemaElement::attributeName)
                                        .toList()
                                        .contains(d.attributeName()))
                                .toList())
                        .build())
                .toList();

        CreateTableRequest ctReq = tables.stream().filter(t -> t.tableName().endsWith("donation")).findAny().orElseThrow();

        CreateTableResponse table = client.createTable(ctReq);
        System.out.println(table);
    }
}
