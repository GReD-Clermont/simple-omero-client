package fr.igred.omero.meta;


import fr.igred.omero.GenericObjectWrapper;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.GroupData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class GroupWrapper extends GenericObjectWrapper<GroupData> {

    /**
     * Constructor of the class GroupWrapper.
     *
     * @param group The object contained in the GroupWrapper.
     */
    public GroupWrapper(GroupData group) {
        super(group);
    }


    /**
     * @return GroupData contained.
     */
    public GroupData asGroupData() {
        return data;
    }


    /**
     * Returns the name of the group.
     *
     * @return See above.
     */
    public String getName() {
        return data.getName();
    }


    /**
     * Sets the name of the group.
     *
     * @param name The name of the group. Mustn't be <code>null</code>.
     *
     * @throws IllegalArgumentException If the name is <code>null</code>.
     */
    public void setName(String name) {
        data.setName(name);
    }


    /**
     * Returns the description of the group.
     *
     * @return See above.
     */
    public String getDescription() {
        return data.getDescription();
    }


    /**
     * Sets the description of the group.
     *
     * @param description The description of the group. Mustn't be <code>null</code>.
     *
     * @throws IllegalArgumentException If the name is <code>null</code>.
     */
    public void setDescription(String description) {
        data.setDescription(description);
    }


    /**
     * Returns the leaders of this group.
     *
     * @return See above.
     */
    public List<ExperimenterWrapper> getLeaders() {
        List<ExperimenterWrapper> experimenters = new ArrayList<>();
        for (ExperimenterData experimenter : data.getLeaders()) {
            experimenters.add(new ExperimenterWrapper(experimenter));
        }
        return experimenters;
    }


    /**
     * Returns the experimenters contained in this group.
     *
     * @return See above.
     */
    public List<ExperimenterWrapper> getExperimenters() {
        List<ExperimenterWrapper> experimenters = new ArrayList<>();
        for (ExperimenterData experimenter : data.getExperimenters()) {
            experimenters.add(new ExperimenterWrapper(experimenter));
        }
        return experimenters;
    }


    /**
     * Sets the experimenters contained in this group.
     *
     * @param experimenters The experimenters list.
     */
    public void setExperimenters(List<ExperimenterWrapper> experimenters) {
        Set<ExperimenterData> set = new HashSet<>();
        for (ExperimenterWrapper experimenter : experimenters) {
            set.add(experimenter.asExperimenterData());
        }
        data.setExperimenters(set);
    }


    /**
     * Returns the list of experimenters that are not owners of the group.
     *
     * @return See above.
     */
    public List<ExperimenterWrapper> getMembersOnly() {
        List<ExperimenterWrapper> experimenters = new ArrayList<>();
        for (ExperimenterData experimenter : data.getMembersOnly()) {
            experimenters.add(new ExperimenterWrapper(experimenter));
        }
        return experimenters;
    }

}
