package com.chen.shortlink.admin.controller;

import com.chen.shortlink.admin.common.convention.result.Result;
import com.chen.shortlink.admin.common.convention.result.Results;
import com.chen.shortlink.admin.dto.req.GroupAddReqDTO;
import com.chen.shortlink.admin.dto.req.GroupUpdateReqDTO;
import com.chen.shortlink.admin.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    /**
     * 添加分组
     * @param groupAddReqDTO
     * @return
     */
    @PostMapping("/api/short-link/v1/group")
    public Result saveGroup(@RequestBody GroupAddReqDTO groupAddReqDTO){
        groupService.saveGroup(groupAddReqDTO);
        return Results.success();
    }

    /**
     * 查询分组列表
     * @return
     */
    @GetMapping("/api/short-link/v1/group")
    public Result listGroup(){
        return Results.success(groupService.listGroup());
    }

    @PutMapping("/api/short-link/admin/v1/group")
    public Result updateGroup(@RequestBody GroupUpdateReqDTO groupUpdateReqDTO){
        groupService.updateGroup(groupUpdateReqDTO);
        return Results.success();
    }

}
