## Kafka Tryouts

Playground for trying out patterns and technique for implementing event driven system.

### Implementing idempotent consumer.

An idempotent consumer is a consumer which handle duplicate message in such a way that there is no undesirable effect on
environment even if consumer receive duplicate message. 

Consider an example where a consumer listen to order created event and deduct money for that order from a customer account.
In case this consumer receive duplicate event(for any reason) as a business we do not want to again deduct money from customer
account for same order. Here we need our consumer to be idempotent.
One option for making a consumer idempotent is keep record of already processed message by uniquely identifying the message
based on some key from the message, every time we receive a message we first check in record have we already processed this 
message or not, in case message was already processed we will skip the message, else the consumer will process the message
and after process will save the unique id of message in record.

References:

https://pradeeploganathan.com/patterns/idempotent-consumer-pattern/#:~:text=Essentially%2C%20being%20idempotent%20means%20that,change%20to%20the%20actor's%20state.&text=Design%20state%20transitioning%20messages%20to,the%20effect%20of%20duplicate%20messages
https://microservices.io/patterns/communication-style/idempotent-consumer.html
http://chrisrichardson.net/post/microservices/patterns/2020/10/16/idempotent-consumer.html