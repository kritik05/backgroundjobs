package background.job.backgroundJobs.model;


import jakarta.persistence.*;

import java.util.Objects;
@Entity
@Table(name = "tenant")
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;


    @Column(nullable = false)
    private String owner;

    @Column(nullable = false)
    private String repo;

    @Column(nullable = false)
    private String pat;

    @Column(nullable = false)
    private String findingindex;

    @Column(nullable = false)
    private String tenant_name;

    private String project_key;

    private String token;

    private String project_name;

    private String username;

    public Tenant() {
    };


    public Tenant(Integer id, String owner, String repo, String pat, String findingindex, String tenant_name, String project_key, String token, String project_name, String username) {
        this.id = id;
        this.owner = owner;
        this.repo = repo;
        this.pat = pat;
        this.findingindex = findingindex;
        this.tenant_name = tenant_name;
        this.project_key = project_key;
        this.token = token;
        this.project_name = project_name;
        this.username = username;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getRepo() {
        return repo;
    }

    public void setRepo(String repo) {
        this.repo = repo;
    }

    public String getPat() {
        return pat;
    }

    public void setPat(String pat) {
        this.pat = pat;
    }

    public String getFindingindex() {
        return findingindex;
    }

    public void setFindingindex(String findingindex) {
        this.findingindex = findingindex;
    }

    public String getTenant_name() {
        return tenant_name;
    }

    public void setTenant_name(String tenant_name) {
        this.tenant_name = tenant_name;
    }

    public String getProject_key() {
        return project_key;
    }

    public void setProject_key(String project_key) {
        this.project_key = project_key;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getProject_name() {
        return project_name;
    }

    public void setProject_name(String project_name) {
        this.project_name = project_name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "Tenant{" +
                "id=" + id +
                ", owner='" + owner + '\'' +
                ", repo='" + repo + '\'' +
                ", pat='" + pat + '\'' +
                ", findingindex='" + findingindex + '\'' +
                ", tenant_name='" + tenant_name + '\'' +
                ", project_key='" + project_key + '\'' +
                ", token='" + token + '\'' +
                ", project_name='" + project_name + '\'' +
                ", username='" + username + '\'' +
                '}';
    }

}