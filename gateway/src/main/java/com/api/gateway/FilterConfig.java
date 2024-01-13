package com.api.gateway;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;

public class FilterConfig extends AbstractGatewayFilterFactory {
	@Override
	public GatewayFilter apply(Object config) {
		return null;
	}
}
