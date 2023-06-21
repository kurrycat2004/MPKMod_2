package io.github.kurrycat.mpkmod.modules;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MPKModuleConfig {
    public String moduleName;
    public String mainClass;

    @JsonCreator
    public MPKModuleConfig(@JsonProperty("moduleName") String moduleName, @JsonProperty("mainClass")String mainClass) {
        this.moduleName = moduleName;
        this.mainClass = mainClass;
    }
}
