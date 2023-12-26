package com.distsystem.interfaces;

import com.distsystem.api.dtos.DistAgentMemoryRow;

/** basic interface for memory service */
public interface AgentMemory extends DistService {

    /** get sequence of this memory in current agent */
    long getMemorySeq();
    /** create single row with memory stats */
    DistAgentMemoryRow createMemoryRow();
}
