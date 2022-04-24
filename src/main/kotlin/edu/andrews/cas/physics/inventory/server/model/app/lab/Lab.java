package edu.andrews.cas.physics.inventory.server.model.app.lab;

import edu.andrews.cas.physics.inventory.server.model.app.IDocumentConversion;
import edu.andrews.cas.physics.inventory.server.model.app.lab.resource.LabResource;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class Lab implements IDocumentConversion {
    private final int id;
    private final String name;
    private final List<LabResource> resources;

    public Lab(int id, String name) {
        this.id = id;
        this.name = name;
        this.resources = new ArrayList<>();
    }

    public Lab(int id, String name, List<LabResource> resources) {
        this.id = id;
        this.name = name;
        this.resources = resources;
    }

    public String getName() {
        return name;
    }

    public List<LabResource> getResources() {
        return resources;
    }

    public void addResource(LabResource resource) {
        if (!this.resources.contains(resource)) this.resources.add(resource);
    }

    public int getId() {
        return id;
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("name", getName())
                .append("resources", getResources().stream().map(LabResource::toDocument).toList());
    }
}
