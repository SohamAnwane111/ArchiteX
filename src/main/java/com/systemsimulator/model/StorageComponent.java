package com.systemsimulator.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class StorageComponent extends Component {
    public enum StorageType {
        BLOCK_STORAGE, OBJECT_STORAGE, FILE_STORAGE
    }

    private StorageType storageType;

    public StorageComponent() {
        super();
    }

    public StorageComponent(String id, String name, StorageType storageType) {
        super(id, name);
        this.storageType = storageType;
    }

    @Override
    public ComponentType getType() {
        return ComponentType.STORAGE;
    }
}
