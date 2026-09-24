package com.example.loan_management.controller;

import com.example.loan_management.dto.CmrcDto;
import com.example.loan_management.entity.Cmrc;
import com.example.loan_management.service.impl.CmrcService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/cmrc")
@CrossOrigin(origins = "http://localhost:4200")
public class CmrcController {
   private final CmrcService cmrcService;
   public CmrcController(CmrcService cmrcService) {
       this.cmrcService = cmrcService;
   }
    @PostMapping
    public CmrcDto create(@RequestBody CmrcDto cmrc) {
        return cmrcService.create(cmrc);
    }

    @GetMapping
    public List<CmrcDto > getAll() {
        return cmrcService.getAll();
    }

    @GetMapping("/{id}")
    public CmrcDto  getById(@PathVariable Long id) {
        return cmrcService.getById(id);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        cmrcService.delete(id);
        return "CMRC deleted successfully";
    }
}
