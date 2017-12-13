# Polibee

Polibee là hệ thống chạy background xử lý logic ở phía background cho các hệ thống muốn sử dụng Event-Driven-Design.

## Các vấn đề cần đáp ứng

Một số vấn đề mà hệ thống Polibee cần đáp ứng:

- Thiết kế theo Domain Driven Design (đang làm và cải tiến dần)
- Hỗ trợ đa kết nối (kết nối tới nhiều database khác nhau)
- Hỗ trợ phân tách Read/Write Repository
- Multi-thread Managing (Sử dụng kafka/database để report)
- Cấu hình kafka threading
- Eventsourcing: https://github.com/confluentinc/bottledwater-pg
- Deploy Strategy (tạm thời: single .jar file)
- Unit-test fully supported and implemented
- Mock được tất cả các kết nối ngoài (kafka, database, redis,...)
- Log, sử dụng log4j (đã dùng)
- Log ra file CSV thời gian xử lý các event và các thông tin profiler khác

- Chuyện gì sẽ xảy ra nếu như các process khác write kafka fails?
--> cần luôn có cơ chế fail-safe cho việc này.
Ví dụ như một đơn hàng khi được tạo ra mà cần có logic gì đó xử lý background
thì phải set isDirty = true. Khi nó được xử lý trong background thì sẽ set isDirty = false
Đến một thời điểm định kỳ background worker sẽ scan lại để tìm các đơn có problem?

Spring AsyncTask: https://spring.io/guides/gs/async-method/
http://www.baeldung.com/spring-async

Test: https://docs.spring.io/spring/docs/current/spring-framework-reference/testing.html

## Các vấn đề đang phát triển

Hỗ trợ Queue hình thức tổng quát (có thể là dùng JMS) - độc lập với Kafka. Hiện tại đang gặp khó khăn trong vấn đề này do cách connect Kafka đến Spring không cùng interface.

Implement Worflow pattern (cùng với State Machine để kiểm soát workflow) 

## Đã có thể đáp ứng

- Sử dụng log4j để log ra file thông tin các event, các tiến trình
- Có sample ghi log ra file csv để profile thời gian xử lý
- DDD Sample
- Multi-threading với khả năng cấu hình một số thông tin cơ bản Thread Pool (Pool-size)
- Có khả năng grateful exit (thoát sau khi đã xử lý hết các công việc hiện tại đang xử lý song song trên các thread khác nhau)
- Sử dụng JUnit4 để viết Unit Test

## Cấu trúc thư mục

1 project module sẽ tương đương với 1 application hoặc một subdomain của một application. Các object bên trong một subdomain thì sẽ có BoundedContext.

Trong một project mỗi package sẽ tương đương với một module của subdomain đó. Trong mỗi module sẽ có các Aggregates, Factories, Repositories tương ứng với suddomain.

vn.gobiz.sample là ví dụ cho một subdomain. Trong mỗi module của subdomain sẽ chứa Domain Layer chứa logic nghiệp vụ của subdomain đó.

vn.gobiz.sampleApp là ví dụ cho một application. Ở đây sẽ chứa Application Layer chứa logic hoạt động của ứng dụng.

polibee.core sẽ chứa lớp Infrastructure và các service Infrastructure. Có thể có những Infrastructure sẽ được tách ra thành module độc lập để Application Layer sử dụng.

## Core
Amazon Web Service Java SDK:
https://github.com/aws/aws-sdk-java

## Setup

Đối với một ứng dụng bất kỳ cần quan tâm đến thư mục resources của nó src/main/resources sẽ có các file example cho cấu hình.

Mặc định bao giờ cũng có:

- hibernate config: cấu hình kết nối database
- log4j config: cấu hình LOG