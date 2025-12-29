# Introduction
The OffestManager provides instant lookup of the latest file offset.
This allows to:
- Query the latest offset instantly without scanning the offset topic.
- Start processing a file from the correct offset immediately.
- Reduce source-connector's startup latency.
