# System Design Simulator - Connection Rules Documentation

This document describes all valid connections between components in the System Design Simulator.

## Component Types
- **CLIENT** - End users or client applications
- **LOAD_BALANCER** - Distributes traffic across multiple instances
- **API_SERVICE** - REST/GraphQL/gRPC API servers
- **DATABASE** - Relational, NoSQL, In-Memory databases
- **CACHE** - Redis, Memcached, in-memory caches
- **QUEUE** - Message queues, event buses, streaming platforms
- **STORAGE** - Object storage, file storage, block storage
- **STREAM_PROCESSOR** - Kafka Streams, Flink, real-time processors
- **BATCH_PROCESSOR** - Spark, Hadoop, batch ETL jobs
- **EXTERNAL_SERVICE** - Third-party APIs, external systems

## Link Types & Connection Rules

### 1. API_CALL
**Purpose:** Synchronous request-response communication

**Valid Connections:**
- ✅ Client → Load Balancer
- ✅ Client → API Service
- ✅ Load Balancer → API Service
- ✅ API Service → Database
- ✅ API Service → Cache
- ✅ API Service → Queue
- ✅ API Service → Storage
- ✅ API Service → API Service (microservice calls)

**Invalid Connections:**
- ❌ Database → Client
- ❌ Cache → API Service
- ❌ Storage → Load Balancer

**Use Cases:**
- User requests through load balancer
- Microservice communication
- Database queries from APIs
- File uploads to storage

---

### 2. STREAM
**Purpose:** Real-time data streaming and event processing

**Valid Connections:**
- ✅ API Service → Queue (publish events)
- ✅ Stream Processor → Queue (publish processed data)
- ✅ Queue → API Service (consume events)
- ✅ Queue → Stream Processor (consume for processing)
- ✅ Queue → Database (stream sink)
- ✅ Stream Processor → Stream Processor (chained processing)
- ✅ Stream Processor → Database (write results)
- ✅ Stream Processor → Storage (write results)

**Invalid Connections:**
- ❌ Database → Queue (use EVENT_FLOW for CDC)
- ❌ Client → Stream Processor
- ❌ Load Balancer → Queue

**Use Cases:**
- Kafka/Kinesis streaming pipelines
- Real-time analytics
- Event-driven microservices
- Stream processing chains (filter → transform → aggregate)

---

### 3. REPLICATION
**Purpose:** Data redundancy and high availability

**Valid Connections:**
- ✅ Database → Database (master-slave, master-master)
- ✅ Cache → Cache (distributed cache clusters)
- ✅ Storage → Storage (geo-redundancy, backup)
- ✅ Queue → Queue (high availability)

**Invalid Connections:**
- ❌ Database → Cache
- ❌ API Service → API Service (use LOAD_BALANCER instead)
- ❌ Any cross-type replication

**Use Cases:**
- Database read replicas
- Multi-region caching
- Storage backup and disaster recovery
- Message queue clustering

---

### 4. ETL_PIPELINE
**Purpose:** Extract, Transform, Load operations for data warehousing

**Valid Connections:**
- ✅ Database → Batch Processor (extract)
- ✅ Storage → Batch Processor (extract from files)
- ✅ External Service → Batch Processor (extract from APIs)
- ✅ Batch Processor → Database (load transformed data)
- ✅ Batch Processor → Storage (export results)
- ✅ Database → Database (data migration, analytics DB)
- ✅ Storage → Database (data ingestion)
- ✅ Database → Storage (archival, export)

**Invalid Connections:**
- ❌ API Service → Batch Processor
- ❌ Queue → Batch Processor (use EVENT_FLOW)
- ❌ Cache → Batch Processor

**Use Cases:**
- Nightly data warehouse ETL
- Data lake ingestion
- Database migrations
- Analytics pipeline

---

### 5. BATCH_TRANSFER
**Purpose:** Bulk data movement (large files, backups)

**Valid Connections:**
- ✅ Batch Processor → Storage (results, exports)
- ✅ Batch Processor → Database (bulk inserts)
- ✅ Storage → Storage (backups, archival)
- ✅ Database → Storage (batch exports, dumps)
- ✅ Storage → Database (batch imports)
- ✅ External Service → Storage (bulk downloads)

**Invalid Connections:**
- ❌ API Service → Storage (use API_CALL)
- ❌ Client → Batch Processor
- ❌ Cache → Storage

**Use Cases:**
- Database backups
- Large file transfers
- Data archival
- Bulk data imports/exports

---

### 6. EVENT_FLOW
**Purpose:** Asynchronous event-driven architecture

**Valid Connections:**
- ✅ API Service → Queue (publish events)
- ✅ Stream Processor → Queue (publish events)
- ✅ Queue → API Service (event handlers)
- ✅ Queue → Stream Processor (event processing)
- ✅ Queue → Batch Processor (batch event processing)
- ✅ API Service → API Service (webhooks, callbacks)
- ✅ Database → Queue (CDC - Change Data Capture)

**Invalid Connections:**
- ❌ Load Balancer → Queue
- ❌ Storage → API Service
- ❌ Cache → Queue

**Use Cases:**
- Event-driven microservices
- Webhook notifications
- Change Data Capture (CDC)
- Saga patterns
- CQRS event sourcing

---

### 7. CACHE_LOOKUP
**Purpose:** Fast data retrieval with cache-aside pattern

**Valid Connections:**
- ✅ API Service → Cache (read/write cache)
- ✅ Load Balancer → Cache (session affinity)
- ✅ Stream Processor → Cache (enrichment lookups)
- ✅ Cache → Database (cache miss fallback)
- ✅ Cache → Storage (large object fallback)

**Invalid Connections:**
- ❌ Database → Cache (populate via API)
- ❌ Queue → Cache
- ❌ Client → Cache

**Use Cases:**
- Read-through cache
- Write-through cache
- Cache-aside pattern
- Session management
- Hot data caching

---

### 8. DATABASE_QUERY
**Purpose:** Direct database read/write operations

**Valid Connections:**
- ✅ API Service → Database (CRUD operations)
- ✅ Batch Processor → Database (batch queries)
- ✅ Stream Processor → Database (enrichment queries)
- ✅ Database → Database (federated queries, cross-DB joins)
- ✅ External Service → Database (analytics, reporting tools)

**Invalid Connections:**
- ❌ Client → Database (use API Service)
- ❌ Load Balancer → Database
- ❌ Queue → Database (use STREAM for sinks)

**Use Cases:**
- Application data access
- Batch analytics queries
- Stream enrichment joins
- Cross-database queries
- External BI tools

---

## Architecture Pattern Examples

### 1. **Three-Tier Web Application**
```
Client → Load Balancer (API_CALL)
Load Balancer → API Service (API_CALL)
API Service → Cache (CACHE_LOOKUP)
Cache → Database (CACHE_LOOKUP - fallback)
API Service → Database (DATABASE_QUERY)
Database → Database (REPLICATION - read replica)
```

### 2. **Event-Driven Microservices**
```
Client → API Service (API_CALL)
API Service → Queue (EVENT_FLOW - publish event)
Queue → API Service 2 (EVENT_FLOW - consume event)
API Service 2 → Database (DATABASE_QUERY)
Database → Queue (EVENT_FLOW - CDC)
```

### 3. **Real-Time Analytics Pipeline**
```
API Service → Queue (STREAM - events)
Queue → Stream Processor (STREAM - consume)
Stream Processor → Stream Processor (STREAM - processing chain)
Stream Processor → Database (STREAM - sink)
Stream Processor → Cache (CACHE_LOOKUP - enrichment)
```

### 4. **Data Warehouse ETL**
```
Database → Batch Processor (ETL_PIPELINE - extract)
External Service → Batch Processor (ETL_PIPELINE - extract)
Batch Processor → Database (ETL_PIPELINE - load to DW)
Database → Storage (BATCH_TRANSFER - archival)
```

### 5. **Highly Available System**
```
Client → Load Balancer (API_CALL)
Load Balancer → API Service (API_CALL)
API Service → Database (DATABASE_QUERY)
Database → Database (REPLICATION - HA)
API Service → Cache (CACHE_LOOKUP)
Cache → Cache (REPLICATION - distributed)
```

## Adding Custom Rules

To add a new connection rule:

1. Create a new class implementing `ConnectionRule`:
```java
public class MyCustomRule implements ConnectionRule {
    @Override
    public boolean isValid(Component source, Component target, LinkType linkType) {
        // Your validation logic
        return true/false;
    }
    
    @Override
    public LinkType getLinkType() {
        return LinkType.YOUR_LINK_TYPE;
    }
    
    @Override
    public String getDescription() {
        return "Description of your rule";
    }
}
```

2. Register it in `ConnectionRuleRegistry`:
```java
registerRule(new MyCustomRule());
```

## Best Practices

1. ✅ **Use Load Balancers** between clients and API services for scalability
2. ✅ **Add Caching** for frequently accessed data
3. ✅ **Use Queues** for asynchronous processing and decoupling
4. ✅ **Replicate** critical components for high availability
5. ✅ **Separate Read/Write** paths with CQRS when needed
6. ✅ **Use Stream Processing** for real-time analytics
7. ✅ **Use Batch Processing** for large-scale data transformations
8. ❌ **Avoid** direct client-to-database connections
9. ❌ **Avoid** circular dependencies without queues
10. ❌ **Avoid** mixing synchronous and asynchronous patterns unnecessarily

---

Generated by System Design Simulator v1.0

