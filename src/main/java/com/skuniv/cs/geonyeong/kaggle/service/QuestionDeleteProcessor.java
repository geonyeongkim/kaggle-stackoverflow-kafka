package com.skuniv.cs.geonyeong.kaggle.service;

import static com.skuniv.cs.geonyeong.kaggle.constant.KafkaConsumerConstant.CONSUME_WAIT_TIME;
import static com.skuniv.cs.geonyeong.kaggle.constant.KafkaConsumerConstant.POLL_SECOND;

import com.skuniv.cs.geonyeong.kaggle.dao.PostDao;
import com.skuniv.cs.geonyeong.kaggle.enums.KafkaTopicType;
import com.skuniv.cs.geonyeong.kaggle.utils.KafkaConsumerFactoryUtil;
import com.skuniv.cs.geonyeong.kaggle.vo.avro.AvroQuestion;
import java.time.Duration;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class QuestionDeleteProcessor implements InitializingBean, DisposableBean, Processor {

    private final PostDao postDao;
    private KafkaConsumer<String, AvroQuestion> consumer;

    @Override
    public void afterPropertiesSet() throws Exception {
        consumer = KafkaConsumerFactoryUtil.createKafkaConsumer(
            Arrays.asList(new KafkaTopicType[]{KafkaTopicType.QUESTION_DELETE}));
    }

    @Override
    public void destroy() {
        consumer.close();
    }

    @Override
    public void start() {
        while (true) {
            ConsumerRecords<String, AvroQuestion> records = consumer
                .poll(Duration.ofSeconds(POLL_SECOND));
            if (records.isEmpty()) {
                try {
                    Thread.sleep(CONSUME_WAIT_TIME);
                } catch (InterruptedException e) {
                    log.error("InterruptedException => {}", e);
                }
            }
            postDao.deleteQuestion(records);
        }
    }

    @Override
    public KafkaTopicType getKafkaTopicType() {
        return KafkaTopicType.QUESTION_DELETE;
    }
}
