package org.jenkinsci.plugins.qywechatwatcher;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.util.logging.Logger;


public class WatcherJobProperty extends JobProperty<Job<?, ?>> {
    private static final Logger LOGGER = Logger.getLogger(WatcherJobProperty.class.getName());

    private final String watcherAddresses;

    @DataBoundConstructor
    public WatcherJobProperty(final String watcherAddresses) {
        this.watcherAddresses = watcherAddresses;
    }

    public String getWatcherAddresses() {
        return watcherAddresses;
    }

    @Extension
    public static class DescriptorImpl extends JobPropertyDescriptor {

        @Override
        public boolean isApplicable(@SuppressWarnings("rawtypes") Class<? extends Job> jobType) {
            return true;
        }

        @Override
        public JobProperty<?> newInstance(final StaplerRequest req, final JSONObject formData) throws FormException {

            final JSONObject watcherData = formData.getJSONObject("watcherEnabled");
            if (watcherData.isNullObject()) return null;

            final String addresses = watcherData.getString("watcherAddresses");
            if (addresses == null || addresses.isEmpty()) return null;

            return new WatcherJobProperty(addresses);
        }

        public FormValidation doCheckWatcherAddresses(@QueryParameter String value) {
            LOGGER.info("doCheckWatcherAddresses: " + value);
            return FormValidation.ok();
        }

        @Override
        public String getDisplayName() {
            return "Qywechat when Job configuration changes";
        }
    }
}
