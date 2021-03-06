/*
 * BSD 3-Clause License
 *
 * Copyright © 2020, viadee Unternehmensberatung AG
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of the copyright holder nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.viadee.bpm.vPAV.processing.model.data;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import soot.toolkits.graph.Block;

import java.util.List;

/**
 * 
 * helper class storing information for data-flow analysis assigns ProcessVariables to basic blocks of the control-flow
 * graph
 * 
 *
 */
public class VariableBlock {

    private Block block;

    private List<ProcessVariableOperation> usedProcessVariables;

    public VariableBlock(Block block, List<ProcessVariableOperation> pvs) {
        this.block = block;
        this.usedProcessVariables = pvs;
    }

    public void setBlock(Block block) {

        this.block = block;
    }

    public Block getBlock() {
        return block;
    }

    public List<ProcessVariableOperation> getAllProcessVariables() {
        return usedProcessVariables;
    }

    public void addProcessVariable(ProcessVariableOperation processVariable) {
        this.usedProcessVariables.add(processVariable);
    }

    public ListMultimap<String, ProcessVariableOperation> getProcessVariablesMapped() {

    	ListMultimap<String, ProcessVariableOperation> variables = ArrayListMultimap.create();
        for (ProcessVariableOperation pv : usedProcessVariables) {
            variables.put(pv.getName(), pv);
        }

        return variables;
    }

}
