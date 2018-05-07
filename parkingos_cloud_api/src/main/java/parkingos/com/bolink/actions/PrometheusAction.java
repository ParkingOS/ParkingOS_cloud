package parkingos.com.bolink.actions;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.StringWriter;

/**
 * 给prometheus暴露监控数据的接口
 * Created by waynelu on 2018/3/29.
 */
@Controller
public class PrometheusAction {
    private CollectorRegistry registry = CollectorRegistry.defaultRegistry;
    /**
     * 从registry 里获取数据TextFormat格式返回
     * @return
     */
    @RequestMapping(value = "/prometheus", method = RequestMethod.GET)
    @ResponseBody
    public String prometheus() {
        StringWriter stringWriter = new StringWriter();
        try {
            TextFormat.write004(stringWriter, registry.metricFamilySamples());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return stringWriter.toString();
    }


}
