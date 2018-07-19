package com.github.huksley.app.system;

import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.system.ApplicationPid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Utility class for mapped diagnostic context.
 * 
 * <code>
 * MDC.var("userId", "test_user").log(() -> log.warn("Error accessing resource - denied!"));
 * or
 * MDC.request().security().thread().jvm().log(() -> log.warn("XXXX"));
 * or
 * MDC.def().log(logger).info("My message");
 * </code>
 */
public class MDC {
	private static String pid;
	
	public static class MDCInstance {
		/**
		 * Добавляет свободные переменные (сериализуемые в String)
		 */
		public MDCInstance var(String name, Object value) {
			if (value != null) {
				org.slf4j.MDC.put(name, value.toString());
			} else {
				org.slf4j.MDC.remove(name);
			}
			return this;
		}
		
		/**
		 * Добавляет свободные переменные (сериализуемые в String)
		 */
		public MDCInstance vars(Object... kv) {
	        for (int i = 0; i < kv.length; i++) {
	            String n = (String) kv[i];
	            Object v = kv[i + 1];
	            var(n, v);
	            i ++;
	        }
	        return this;
		}

		/**
		 * Вызывает логгирование и потом очищает MDC контекст.
		 */
		public MDCInstance log(Runnable r) {
			r.run();
			org.slf4j.MDC.clear();
			return this;
		}
		
		/**
		 * Для возможности вызывать log().info(...)
		 */
		public <T> T log(T log) {
		    return log;
		}
		
		/**
		 * Добавляет имя авторизованного пользователя если он авторизован.
		 */
		public MDCInstance security() {
			SecurityContext context = SecurityContextHolder.getContext();
			Authentication auth = context.getAuthentication();
			if (auth != null && auth.isAuthenticated()) {
				var("userId", auth.getPrincipal());
			}
			return this;
		}
		
		/**
		 * Добавляет переменные текущего запроса HTTP если в данный момент выполняется в рамках запроса
		 */
		public MDCInstance request() {
			RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		    if (requestAttributes instanceof ServletRequestAttributes) {
		        HttpServletRequest request = ((ServletRequestAttributes)requestAttributes).getRequest();
		        if (request != null) {
		        	var("requestPath", request.getContextPath());
		        	String ip = request.getHeader("X-Real-IP");
		        	var("removeAddr", ip != null ? ip : request.getRemoteAddr());
		        	String traceId = request.getHeader("X-Trace-ID");
		        	if (traceId != null) {
		        		var("traceId", traceId);
		        	}
		        }
		    }
		    return this;
		}

		/**
		 * Добавляем переменные текущего потока.
		 */
		public MDCInstance thread() {
			Thread currentThread = Thread.currentThread();
			var("threadName", currentThread.getName());
		    return this;
		}

		/**
		 * Добавляет переменные процесса Java
		 */
		public MDCInstance jvm() {
			if (pid == null) {
				ApplicationPid apid = new ApplicationPid();
				pid = apid.toString();
			}
			var("processId", pid != null && !pid.equals("???") ? pid : null);
		    return this;
		}
		
		/**
		 * Устанавливает указанные ENV_VARIABLE -> MDC key если они есть в переменных окружения
		 */
		public MDCInstance env(String... envmap) {
			for (int i = 0; i < envmap.length; i++) {
	            String n = (String) envmap[i];
	            String nn = envmap[i + 1];
	            String v = System.getenv(n);
	            if (v != null) {
	            	var(nn, v);
	            }
	            i ++;
	        }
			return this;
		}
		
		/**
		 * Устанавливает указанные System.getProperty если они есть в переменных окружения
		 */
		public MDCInstance sysprop(String... systemProperties) {
			for (int i = 0; i < systemProperties.length; i++) {
	            String n = (String) systemProperties[i];
	            String v = System.getProperty(n);
	            if (v != null) {
	            	var(n, v);
	            }
	        }
			return this;
		}
	}
	
	/**
	 * Создает объект по умолчанию, вызывая все возможные источники атрибутов MDC
	 */
	public static MDCInstance def() {
	    return new MDCInstance().jvm().request().thread().security();
	}

	/**
	 * Добавляет свободные переменные (сериализуемые в String)
	 */
	public static MDCInstance var(String name, Object value) {		
		return new MDCInstance().var(name, value);
	}
	
	/**
	 * Добавляет свободные переменные (сериализуемые в String)
	 */
	public static MDCInstance vars(Object... kv) {      
        return new MDCInstance().vars(kv);
	}
	
	/**
	 * Добавляет имя авторизованного пользователя если он авторизован.
	 */
	public static MDCInstance security() {
		return new MDCInstance().security();
	}
	
	/**
	 * Добавляем переменные текущего потока.
	 */
	public static MDCInstance thread() {
		return new MDCInstance().thread();
	}
	
	/**
	 * Добавляет переменные текущего запроса HTTP если в данный момент выполняется в рамках запроса
	 */
	public static MDCInstance request() {
		return new MDCInstance().request();
	}
	
	/**
	 * Добавляет переменные процесса Java
	 */
	public static MDCInstance jvm() {
		return new MDCInstance().jvm();
	}
	
	/**
	 * Устанавливает указанные ENV_VARIABLE -> MDC key если они есть в переменных окружения
	 */
	public static MDCInstance env(String... mapenv) {
		return new MDCInstance().env(mapenv);
	}
	
	/**
	 * Устанавливает указанные System.getProperty если они есть в переменных окружения
	 */
	public MDCInstance sysprop(String... systemProperties) {		
		return new MDCInstance().sysprop(systemProperties);
	}
}