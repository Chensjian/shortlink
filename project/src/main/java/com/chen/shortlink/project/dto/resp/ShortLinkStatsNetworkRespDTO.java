package com.chen.shortlink.project.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortLinkStatsNetworkRespDTO {

    /**
     * 访问量
     */
    private Integer cnt;


    /**
     * 网络类型
     */
    private String network;

    /**
     * 占比
     */
    private Double ratio;
}
