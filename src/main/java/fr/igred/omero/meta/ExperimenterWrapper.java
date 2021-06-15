package fr.igred.omero.meta;


import fr.igred.omero.GenericObjectWrapper;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.GroupData;

import java.util.ArrayList;
import java.util.List;


public class ExperimenterWrapper extends GenericObjectWrapper<ExperimenterData> {

    /**
     * Constructor of the class ExperimenterWrapper.
     *
     * @param experimenter The experimenter contained in the ExperimenterWrapper.
     */
    public ExperimenterWrapper(ExperimenterData experimenter) {
        super(experimenter);
    }


    /**
     * @return ExperimenterData contained.
     */
    public ExperimenterData asExperimenterData() {
        return data;
    }


    /**
     * Returns the first name of the experimenter.
     *
     * @return see above.
     */
    public String getFirstName() {
        return data.getFirstName();
    }


    /**
     * Sets the first name of the experimenter.
     *
     * @param firstName The value to set.
     */
    public void setFirstName(String firstName) {
        data.setFirstName(firstName);
    }


    /**
     * Returns the last name of the experimenter.
     *
     * @return see above.
     */
    public String getLastName() {
        return data.getLastName();
    }


    /**
     * Sets the last name of the experimenter.
     *
     * @param lastName The value to set.
     */
    public void setLastName(String lastName) {
        data.setLastName(lastName);
    }


    /**
     * Returns the last name of the experimenter.
     *
     * @return see above.
     */
    public String getUserName() {
        return data.getUserName();
    }


    /**
     * Returns the e-mail of the experimenter.
     *
     * @return see above.
     */
    public String getEmail() {
        return data.getEmail();
    }


    /**
     * Sets the e-mail of the experimenter.
     *
     * @param email The value to set.
     */
    public void setEmail(String email) {
        data.setEmail(email);
    }


    /**
     * Returns the institution where the experimenter works.
     *
     * @return see above.
     */
    public String getInstitution() {
        return data.getInstitution();
    }


    /**
     * Sets the institution where the experimenter works.
     *
     * @param institution The value to set.
     */
    public void setInstitution(String institution) {
        data.setInstitution(institution);
    }


    /**
     * Returns the groups the experimenter is a member of.
     *
     * @return See above.
     */
    public List<GroupWrapper> getGroups() {
        List<GroupData> groups = data.getGroups();

        List<GroupWrapper> groupWrappers = new ArrayList<>(groups.size());
        for (GroupData group : groups) {
            groupWrappers.add(new GroupWrapper(group));
        }
        return groupWrappers;
    }


    /**
     * Returns the default Group for this Experimenter
     *
     * @return See above.
     */
    public GroupWrapper getDefaultGroup() {
        return new GroupWrapper(data.getDefaultGroup());
    }


    /**
     * Returns the middle name of the experimenter.
     *
     * @return see above.
     */
    public String getMiddleName() {
        return data.getMiddleName();
    }


    /**
     * Sets the middle name of the experimenter.
     *
     * @param middleName The value to set.
     */
    public void setMiddleName(String middleName) {
        data.setMiddleName(middleName);
    }


    /**
     * Returns <code>true</code> if the experimenter is active,
     * <code>false</code> otherwise.
     *
     * @return See above.
     */
    public boolean isActive() {
        return data.isActive();
    }


    /**
     * Checks if supplied group id matches any group to which the current experimenter belongs to.
     *
     * @param groupId The id of the group.
     *
     * @return boolean <code>true</code>/<code>false</code> depending if matching id found
     */
    public boolean isMemberOfGroup(long groupId) {
        return data.isMemberOfGroup(groupId);
    }


    /**
     * Returns <code>true</code> if the user is connected via LDAP.
     *
     * @return See above.
     */
    public boolean isLDAP() {
        return data.isLDAP();
    }

}
