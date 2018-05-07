package parkingos.com.bolink.prometheus;

import io.prometheus.client.hotspot.DefaultExports;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;
@Component
public class PmExplore implements BeanFactoryPostProcessor {
   public void defaultExports(){
	   DefaultExports.initialize();
   }

@Override
public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
		throws BeansException {
	System.out.println(".................开始加载prometheus exploer .................");
	defaultExports();
	// TODO Auto-generated method stub
	
}
}
