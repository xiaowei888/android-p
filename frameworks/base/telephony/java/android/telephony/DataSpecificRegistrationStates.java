package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;

/// M: [Network][C2K] Telephony add-on.@{
import android.telephony.Rlog;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
/// @}

import java.util.Objects;


/**
 * Class that stores information specific to data network registration.
 * @hide
 */
public class DataSpecificRegistrationStates implements Parcelable{
    /// M: [Network][C2K] Telephony add-on.@{
    protected static final String LOG_TAG = "DataSpecificRegistrationStates";
    /// @}

    /**
     * The maximum number of simultaneous Data Calls that
     * must be established using setupDataCall().
     */
    public final int maxDataCalls;

    protected DataSpecificRegistrationStates(int maxDataCalls) {
        this.maxDataCalls = maxDataCalls;
    }

    protected DataSpecificRegistrationStates(Parcel source) {
        maxDataCalls = source.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(maxDataCalls);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return "DataSpecificRegistrationStates {" + " mMaxDataCalls=" + maxDataCalls + "}";
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxDataCalls);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || !(o instanceof DataSpecificRegistrationStates)) {
            return false;
        }

        DataSpecificRegistrationStates other = (DataSpecificRegistrationStates) o;
        return this.maxDataCalls == other.maxDataCalls;
    }

    public static final Parcelable.Creator<DataSpecificRegistrationStates> CREATOR =
            new Parcelable.Creator<DataSpecificRegistrationStates>() {
                @Override
                public DataSpecificRegistrationStates createFromParcel(Parcel source) {
                    /// M: [Network][C2K] Telephony add-on.@{
                    return makeDataSpecificRegistrationStates(source);
                    /// @}
                }

                @Override
                public DataSpecificRegistrationStates[] newArray(int size) {
                    return new DataSpecificRegistrationStates[size];
                }
            };

    /// M: [Network][C2K]Telephony add-on provide create DataSpecificRegistrationStates function.@{
    /**
     * Create DataSpecificRegistrationStates.
     * @param source The Parcel to read the object's data from.
     * @return Returns a new instance of the DataSpecificRegistrationStates class.
     */
    private static final DataSpecificRegistrationStates makeDataSpecificRegistrationStates(
            Parcel source) {
        DataSpecificRegistrationStates instance;
        String className = "mediatek.telephony.MtkDataSpecificRegistrationStates";
        Class<?> clazz = null;
        try {
            clazz = Class.forName(className);
            Constructor clazzConstructfunc = clazz.getConstructor(Parcel.class);
            clazzConstructfunc.setAccessible(true);
            instance = (DataSpecificRegistrationStates) clazzConstructfunc.newInstance(source);
            // Usually it should not run into these exceptions
        } catch (InstantiationException e) {
            e.printStackTrace();
            Rlog.e(LOG_TAG, "InstantiationException! Used AOSP!");
            instance = new DataSpecificRegistrationStates(source);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            Rlog.e(LOG_TAG, "InvocationTargetException! Used AOSP!");
            instance = new DataSpecificRegistrationStates(source);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Rlog.e(LOG_TAG, "IllegalAccessException! Used AOSP!");
            instance = new DataSpecificRegistrationStates(source);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            Rlog.e(LOG_TAG, "NoSuchMethodException! Used AOSP!");
            instance = new DataSpecificRegistrationStates(source);
        } catch (Exception e) {
            // No MtkDataSpecificRegistrationStates! Used AOSP instead!
            instance = new DataSpecificRegistrationStates(source);
        }
        return instance;
    }
    /// @}
}