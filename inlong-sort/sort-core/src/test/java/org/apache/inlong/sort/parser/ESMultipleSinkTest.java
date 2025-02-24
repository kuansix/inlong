/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.inlong.sort.parser;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.inlong.sort.formats.common.VarBinaryFormatInfo;
import org.apache.inlong.sort.parser.impl.FlinkSqlParser;
import org.apache.inlong.sort.parser.result.ParseResult;
import org.apache.inlong.sort.protocol.FieldInfo;
import org.apache.inlong.sort.protocol.GroupInfo;
import org.apache.inlong.sort.protocol.StreamInfo;
import org.apache.inlong.sort.protocol.enums.KafkaScanStartupMode;
import org.apache.inlong.sort.protocol.node.Node;
import org.apache.inlong.sort.protocol.node.extract.KafkaExtractNode;
import org.apache.inlong.sort.protocol.node.format.CanalJsonFormat;
import org.apache.inlong.sort.protocol.node.format.RawFormat;
import org.apache.inlong.sort.protocol.node.load.ElasticsearchLoadNode;
import org.apache.inlong.sort.protocol.transformation.FieldRelation;
import org.apache.inlong.sort.protocol.transformation.relation.NodeRelation;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Test for {@link ElasticsearchLoadNode}
 */
public class ESMultipleSinkTest {

    private KafkaExtractNode buildKafkaExtractNode() {
        List<FieldInfo> fields = Collections.singletonList(new FieldInfo("raw", new VarBinaryFormatInfo()));
        return new KafkaExtractNode("1", "kafka_input", fields,
                null, null, "kafka_raw", "localhost:9092",
                new RawFormat(), KafkaScanStartupMode.EARLIEST_OFFSET, null,
                "test_group", null, null);
    }

    /**
     * Build node relation
     *
     * @param inputs extract node
     * @param outputs load node
     * @return node relation
     */
    private NodeRelation buildNodeRelation(List<Node> inputs, List<Node> outputs) {
        List<String> inputIds = inputs.stream().map(Node::getId).collect(Collectors.toList());
        List<String> outputIds = outputs.stream().map(Node::getId).collect(Collectors.toList());
        return new NodeRelation(inputIds, outputIds);
    }

    private ElasticsearchLoadNode buildESLoadNodeWithMultipleSink(String indexPattern) {
        List<FieldInfo> fields = Collections.singletonList(new FieldInfo("raw", new VarBinaryFormatInfo()));
        List<FieldRelation> relations = Collections
                .singletonList(new FieldRelation(new FieldInfo("raw", new VarBinaryFormatInfo()),
                        new FieldInfo("raw", new VarBinaryFormatInfo())));
        Map<String, String> properties = new LinkedHashMap<>();
        properties.put("dirty.side-output.connector", "log");
        properties.put("dirty.ignore", "true");
        properties.put("dirty.side-output.enable", "true");
        properties.put("dirty.side-output.format", "csv");
        properties.put("dirty.side-output.labels",
                "SYSTEM_TIME=${SYSTEM_TIME}&DIRTY_TYPE=${DIRTY_TYPE}&database=${database}&table=${table}");
        properties.put("dirty.identifier", "${database}-${table}-${SYSTEM_TIME}");
        properties.put("dirty.side-output.s3.bucket", "s3-test-bucket");
        properties.put("dirty.side-output.s3.endpoint", "s3.test.endpoint");
        properties.put("dirty.side-output.s3.key", "dirty/test");
        properties.put("dirty.side-output.s3.region", "region");
        properties.put("dirty.side-output.s3.access-key-id", "access_key_id");
        properties.put("dirty.side-output.s3.secret-key-id", "secret_key_id");
        return new ElasticsearchLoadNode("2", "es_output", fields, relations,
                null, null, 1, properties, "mock",
                "localhost:8030", "root", "000000", null,
                null, 1, true, new CanalJsonFormat(), indexPattern);
    }

    /**
     * Test kafka to ES with multiple sink enable
     *
     * @throws Exception The exception may be thrown when executing
     */
    @Test
    public void testESMultipleSinkParse() throws Exception {
        EnvironmentSettings settings = EnvironmentSettings
                .newInstance()
                .useBlinkPlanner()
                .inStreamingMode()
                .build();
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        env.enableCheckpointing(10000);
        env.disableOperatorChaining();
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env, settings);
        Node inputNode = buildKafkaExtractNode();
        Node outputNode = buildESLoadNodeWithMultipleSink("${database}_${table}");
        StreamInfo streamInfo = new StreamInfo("1", Arrays.asList(inputNode, outputNode),
                Collections.singletonList(buildNodeRelation(Collections.singletonList(inputNode),
                        Collections.singletonList(outputNode))));
        GroupInfo groupInfo = new GroupInfo("1", Collections.singletonList(streamInfo));
        FlinkSqlParser parser = FlinkSqlParser.getInstance(tableEnv, groupInfo);
        ParseResult result = parser.parse();
        Assert.assertTrue(result.tryExecute());
    }
}