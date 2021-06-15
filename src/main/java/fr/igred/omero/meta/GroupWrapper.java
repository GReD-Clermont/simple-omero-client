package fr.igred.omero.meta;


import fr.igred.omero.GenericObjectWrapper;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.GroupData;

import java.util.ArrayList;
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
        Set<ExperimenterData> leaders = data.getLeaders();

        List<ExperimenterWrapper> wrappers = new ArrayList<>(leaders.size());
        for (ExperimenterData leader : leaders) {
            wrappers.add(new ExperimenterWrapper(leader));
        }
        return wrappers;
    }


    /**
     * Returns the experimenters contained in this group.
     *
     * @return See above.
     */
    public List<ExperimenterWrapper> getExperimenters() {
        Set<ExperimenterData> experimenters = data.getExperimenters();

        List<ExperimenterWrapper> wrappers = new ArrayList<>(experimenters.size());
        for (ExperimenterData experimenter : experimenters) {
            wrappers.add(new ExperimenterWrapper(experimenter));
        }
        return wrappers;
    }


    /**
     * Returns the list of experimenters that are not owners of the group.
     *
     * @return See above.
     */
    public List<ExperimenterWrapper> getMembersOnly() {
        Set<ExperimenterData> members = data.getMembersOnly();

        List<ExperimenterWrapper> wrappers = new ArrayList<>(members.size());
        for (ExperimenterData member : members) {
            wrappers.add(new ExperimenterWrapper(member));
        }
        return wrappers;
    }

}
