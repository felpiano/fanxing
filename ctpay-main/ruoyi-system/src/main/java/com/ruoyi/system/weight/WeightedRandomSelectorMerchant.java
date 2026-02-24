package com.ruoyi.system.weight;

import com.google.common.collect.Lists;
import com.ruoyi.system.domain.vo.SuitableQrcodeVO;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class WeightedRandomSelectorMerchant {

    public static SuitableQrcodeVO selectWeightedRandomly(List<SuitableQrcodeVO> data) {
        // 构建Weighted对象列表
        List<Weighted<SuitableQrcodeVO>> weightedData = Lists.newArrayList();
        int countWeight = 0;
        for (SuitableQrcodeVO item : data) {
            if(item.getWeight() != null){
                int weight = item.getWeight();
                countWeight = countWeight + weight;
                weightedData.add(new Weighted<>(item, weight));
            }
        }
        if(countWeight==0){
            Collections.shuffle(data);
            return data.get(0);
        }
        // 调用chooseWeightedRandomly方法进行随机选择
        Weighted<SuitableQrcodeVO> selected = chooseWeightedRandomly(weightedData);
        return selected.getElement();
    }



    public static <T> Weighted<T> chooseWeightedRandomly(List<Weighted<T>> weightedItems) {
        double totalWeight = weightedItems.stream().mapToDouble(Weighted::getWeight).sum();
        double randomWeight = Math.random() * totalWeight;

        for (Weighted<T> item : weightedItems) {
            randomWeight -= item.getWeight();
            if (randomWeight <= 0) {
                return item;
            }
        }

        // 如果出现异常情况，返回第一个元素
        return weightedItems.get(0);
    }
}

