
package org.sqx.mnistclassification;

import org.sqx.mnistclassification.model.MnistModel;
import org.sqx.mnistclassification.service.TrainingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class MnistClassificationApplication implements CommandLineRunner {

    @Autowired
    private TrainingService trainingService;

    public static void main(String[] args) {
        SpringApplication.run(MnistClassificationApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== MNIST调参功能测试 ===");
        
        // 1. 获取当前参数
        System.out.println("1. 当前参数配置:");
        Map<String, Object> currentParams = trainingService.getCurrentParameters();
        printParameters(currentParams);
        
        // 2. 测试调参功能
        System.out.println("\n2. 测试参数调优:");
        Map<String, Object> params = new HashMap<>();
        params.put("learningRate", 0.001);
        params.put("batchSize", 64);
        params.put("hiddenLayerSizes", new int[]{1024, 512, 256, 128});
        params.put("dropoutRates", new double[]{0.3, 0.3, 0.2});
        params.put("l2Regularization", 0.0001);
        
        Map<String, Object> result = trainingService.tuneParameters(params);
        System.out.println("调参结果: " + result.get("message"));
        
        // 3. 再次获取参数验证是否更新成功
        System.out.println("\n3. 更新后的参数配置:");
        currentParams = trainingService.getCurrentParameters();
        printParameters(currentParams);
        
        System.out.println("\n=== 调参功能测试完成 ===");
    }
    
    private void printParameters(Map<String, Object> params) {
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            if (value instanceof int[]) {
                System.out.print(key + ": [");
                int[] arr = (int[]) value;
                for (int i = 0; i < arr.length; i++) {
                    System.out.print(arr[i]);
                    if (i < arr.length - 1) System.out.print(", ");
                }
                System.out.println("]");
            } else if (value instanceof double[]) {
                System.out.print(key + ": [");
                double[] arr = (double[]) value;
                for (int i = 0; i < arr.length; i++) {
                    System.out.print(arr[i]);
                    if (i < arr.length - 1) System.out.print(", ");
                }
                System.out.println("]");
            } else {
                System.out.println(key + ": " + value);
            }
        }
    }
}
