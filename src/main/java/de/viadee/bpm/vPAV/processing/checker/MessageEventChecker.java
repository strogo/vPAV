/**
 * Copyright © 2017, viadee Unternehmensberatung GmbH
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the viadee Unternehmensberatung GmbH.
 * 4. Neither the name of the viadee Unternehmensberatung GmbH nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <viadee Unternehmensberatung GmbH> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.viadee.bpm.vPAV.processing.checker;

import java.util.ArrayList;
import java.util.Collection;

import org.camunda.bpm.model.bpmn.impl.BpmnModelConstants;
import org.camunda.bpm.model.bpmn.instance.BaseElement;
import org.camunda.bpm.model.bpmn.instance.Event;
import org.camunda.bpm.model.bpmn.instance.Message;
import org.camunda.bpm.model.bpmn.instance.MessageEventDefinition;

import de.viadee.bpm.vPAV.BpmnScanner;
import de.viadee.bpm.vPAV.config.model.Rule;
import de.viadee.bpm.vPAV.processing.CheckName;
import de.viadee.bpm.vPAV.processing.model.data.BpmnElement;
import de.viadee.bpm.vPAV.processing.model.data.CheckerIssue;
import de.viadee.bpm.vPAV.processing.model.data.CriticalityEnum;

public class MessageEventChecker extends AbstractElementChecker {

    public MessageEventChecker(final Rule rule, final BpmnScanner bpmnScanner) {
        super(rule, bpmnScanner);
    }

    /**
     * Check MessageEvents for implementation and messages
     *
     * @return issues
     */
    @Override
    public Collection<CheckerIssue> check(BpmnElement element) {

        final Collection<CheckerIssue> issues = new ArrayList<CheckerIssue>();
        final BaseElement baseElement = element.getBaseElement();

        if (baseElement.getElementType().getTypeName()
                .equals(BpmnModelConstants.BPMN_ELEMENT_END_EVENT)
                || baseElement.getElementType().getTypeName()
                        .equals(BpmnModelConstants.BPMN_ELEMENT_INTERMEDIATE_CATCH_EVENT)
                || baseElement.getElementType().getTypeName()
                        .equals(BpmnModelConstants.BPMN_ELEMENT_INTERMEDIATE_THROW_EVENT)
                || baseElement.getElementType().getTypeName()
                        .equals(BpmnModelConstants.BPMN_ELEMENT_BOUNDARY_EVENT)) {

            checkStartEventInSubProcess(element, issues, baseElement);
        } else if (baseElement.getElementType().getTypeName().equals(BpmnModelConstants.BPMN_ELEMENT_RECEIVE_TASK)) {

            if (baseElement.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_MESSAGE_REF) == null
                    || baseElement.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_MESSAGE_REF).isEmpty()) {
                issues.add(new CheckerIssue(rule.getName(), rule.getRuleDescription(), CriticalityEnum.ERROR,
                        element.getProcessdefinition(), null,
                        baseElement.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_ID),
                        baseElement.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_NAME), null, null, null,
                        "No message has been specified for '" + CheckName.checkName(baseElement) + "'.", null));
            } else {
                if (bpmnScanner.getMessageName(
                        baseElement.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_MESSAGE_REF)) == null
                        || bpmnScanner
                                .getMessageName(
                                        baseElement.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_MESSAGE_REF))
                                .isEmpty()) {
                    issues.add(new CheckerIssue(rule.getName(), rule.getRuleDescription(), CriticalityEnum.ERROR,
                            element.getProcessdefinition(), null,
                            baseElement.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_ID),
                            baseElement.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_NAME), null, null, null,
                            "No message name has been specified for '" + CheckName.checkName(baseElement) + "'.",
                            null));
                }
            }
        } else if (baseElement.getElementType().getTypeName()
                .equals(BpmnModelConstants.BPMN_ELEMENT_START_EVENT)) {

            // Depending on whether the startEvent is part of a subprocess, expressions may be allowed to exist
            if (bpmnScanner.checkStartEvent(baseElement.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_ID))) {
                checkStartEventInSubProcess(element, issues, baseElement);
            } else {
                checkStartEventInProcess(element, issues, baseElement);
            }
        }
        return issues;
    }

    /**
     * Checks for existence of messages in startEvents. Expressions will create an issue due to write/read anomaly
     *
     * @param element
     *            BpmnElement
     * @param issues
     *            Collection of CheckerIssues
     * @param baseElement
     *            BaseElement
     */
    private void checkStartEventInProcess(BpmnElement element, final Collection<CheckerIssue> issues,
            final BaseElement baseElement) {
        final Event event = (Event) baseElement;
        final Collection<MessageEventDefinition> messageEventDefinitions = event
                .getChildElementsByType(MessageEventDefinition.class);
        if (messageEventDefinitions != null) {
            for (MessageEventDefinition eventDef : messageEventDefinitions) {
                if (eventDef != null) {
                    final Message message = eventDef.getMessage();
                    if (message == null) {
                        issues.add(
                                new CheckerIssue(rule.getName(), rule.getRuleDescription(),
                                        CriticalityEnum.ERROR,
                                        element.getProcessdefinition(), null,
                                        baseElement.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_ID),
                                        baseElement.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_NAME),
                                        null,
                                        null, null,
                                        "No message has been specified for '" + CheckName.checkName(baseElement)
                                                + "'.",
                                        null));
                    } else {
                        if (message.getName() == null || message.getName().isEmpty()) {
                            issues.add(new CheckerIssue(rule.getName(), rule.getRuleDescription(),
                                    CriticalityEnum.ERROR,
                                    element.getProcessdefinition(), null,
                                    baseElement.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_ID),
                                    baseElement.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_NAME), null,
                                    null, null,
                                    "No message name has been specified for '"
                                            + CheckName.checkName(baseElement)
                                            + "'.",
                                    null));
                        } else if (message.getName().contains("{") || message.getName().contains("}")) {
                            issues.add(new CheckerIssue(rule.getName(), rule.getRuleDescription(),
                                    CriticalityEnum.ERROR,
                                    element.getProcessdefinition(), null,
                                    baseElement.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_ID),
                                    baseElement.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_NAME), null,
                                    null, null,
                                    "Usage of expressions in MessageStartEvent "
                                            + CheckName.checkName(baseElement) + "is not allowed",
                                    null));
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks for existence of messages and expression in startEvents
     *
     * @param element
     *            BpmnElement
     * @param issues
     *            Collection of CheckerIssues
     * @param baseElement
     *            BaseElement
     */
    private void checkStartEventInSubProcess(BpmnElement element, final Collection<CheckerIssue> issues,
            final BaseElement baseElement) {
        final Event event = (Event) baseElement;
        final Collection<MessageEventDefinition> messageEventDefinitions = event
                .getChildElementsByType(MessageEventDefinition.class);
        if (messageEventDefinitions != null) {
            for (MessageEventDefinition eventDef : messageEventDefinitions) {
                if (eventDef != null) {
                    final Message message = eventDef.getMessage();
                    if (message == null) {
                        issues.add(
                                new CheckerIssue(rule.getName(), rule.getRuleDescription(),
                                        CriticalityEnum.ERROR,
                                        element.getProcessdefinition(), null,
                                        baseElement.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_ID),
                                        baseElement.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_NAME),
                                        null,
                                        null, null,
                                        "No message has been specified for '" + CheckName.checkName(baseElement)
                                                + "'.",
                                        null));
                    } else {
                        if (message.getName() == null || message.getName().isEmpty()) {
                            issues.add(new CheckerIssue(rule.getName(), rule.getRuleDescription(),
                                    CriticalityEnum.ERROR,
                                    element.getProcessdefinition(), null,
                                    baseElement.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_ID),
                                    baseElement.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_NAME), null,
                                    null, null,
                                    "No message name has been specified for '"
                                            + CheckName.checkName(baseElement)
                                            + "'.",
                                    null));
                        }
                    }
                }
            }
        }
    }

}
