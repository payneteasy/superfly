package com.payneteasy.superfly.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.payneteasy.superfly.api.ActionDescription;
import com.payneteasy.superfly.api.SSOService;
import com.payneteasy.superfly.client.exception.CollectionException;

/**
 * Class that, upon instantiation and initialization in a Spring Application
 * Context, automatically sends subsystem data to SSOService.
 * 
 * @author Roman Puchkovskiy
 */
public class SuperflyDataSender {
	
	private static final Logger logger = LoggerFactory.getLogger(SuperflyDataSender.class);
	
	private SSOService ssoService;
	private ActionDescriptionCollector actionDescriptionCollector;
	private String subsystemIdentifier = null;
	private long delay = 0;
	private StringTransformer[] transformers = new StringTransformer[0];
	private boolean autoSend = true;

	public void setSsoService(SSOService ssoService) {
		this.ssoService = ssoService;
	}

	public void setActionDescriptionCollector(
			ActionDescriptionCollector actionDescriptionCollector) {
		this.actionDescriptionCollector = actionDescriptionCollector;
	}

	public String getSubsystemIdentifier() {
		return subsystemIdentifier;
	}

	public void setSubsystemIdentifier(String subsystemIdentifier) {
		this.subsystemIdentifier = subsystemIdentifier;
	}

	public void setDelay(long delay) {
		this.delay = delay;
	}

	public void setTransformers(StringTransformer[] transformers) {
		this.transformers = transformers;
	}

	public void setAutoSend(boolean autoSend) {
		this.autoSend = autoSend;
	}

	@PostConstruct
	public void afterPropertiesSet() throws Exception {
		if (autoSend) {
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					send();
				}
			}, delay);
		}
	}

	protected ActionDescription[] obtainActionDescriptions() throws CollectionException {
		List<ActionDescription> actions = actionDescriptionCollector.collect();
		for (ActionDescription action : actions) {
			action.setName(applyTransformers(action.getName()));
		}
		return actions.toArray(new ActionDescription[actions.size()]);
	}

	protected String applyTransformers(String name) {
		for (StringTransformer transformer : transformers) {
			name = transformer.transform(name);
		}
		return name;
	}

	public void send() {
		try {
            final ActionDescription[] actionDescriptions = obtainActionDescriptions();
            if (logger.isDebugEnabled()) {
                logger.debug("Sending the following actions: {}", getActionsStringForLog(actionDescriptions));
            }
            ssoService.sendSystemData(getSubsystemIdentifier(),
                    actionDescriptions);
		} catch (CollectionException e) {
			logger.error("Cannot send subsystem data", e);
		}
	}

    private String getActionsStringForLog(ActionDescription[] actionDescriptions) {
        List<String> names = new ArrayList<String>(actionDescriptions.length);
        for (ActionDescription description : actionDescriptions) {
            names.add(description.getName());
        }

        Collections.sort(names);

        return names.toString();
    }

}
