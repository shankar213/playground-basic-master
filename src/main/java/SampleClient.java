import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Patient;

import java.text.SimpleDateFormat;
import java.util.*;

public class SampleClient {

    public static void main(String[] theArgs) {

        // Create a FHIR client
        FhirContext fhirContext = FhirContext.forR4();
        IGenericClient client = fhirContext.newRestfulGenericClient("http://hapi.fhir.org/baseR4");
        client.registerInterceptor(new LoggingInterceptor(false));

        // Search for Patient resources
        Bundle response = client
                .search()
                .forResource("Patient")
                .where(Patient.FAMILY.matches().value("SMITH"))
                .returnBundle(Bundle.class)
                .execute();

        // extract patient data as a List to make it easily reusable.
        List<Patient> patients = collectPatientDataFromResponse(response);

        System.out.printf("%-20s  %-20s %s \n", "FIRST NAME", "LAST NAME", "BIRTH DATE");
        printPatientDetails(patients);    // print data of patients with labels

        /* sort the patient data */
        patients.sort(new SortByFirstName());

        System.out.println("\n---------Patient data Sorted by First Name --------------");
        System.out.printf("%-20s  %-20s %s \n", "FIRST NAME", "LAST NAME", "BIRTH DATE");
        // print data of patients with labels
        printPatientDetails(patients);

    }

    /**
     * extracts the patients details from bundle and returns as a List
     * @param bundle bundle data containing patient data
     * @return List of patients fetched from the bundle
     */
    private static List<Patient> collectPatientDataFromResponse(Bundle bundle) {
        List<Patient> patients = new ArrayList<>();
        for (Bundle.BundleEntryComponent be : bundle.getEntry()) {
            if (be.getResource() instanceof Patient) { //safe check to avoid casting errors
                patients.add((Patient) be.getResource());
            }
        }
        return patients;
    }

    /**
     * print the basic details(name and birth date) of patient
     * @param data patients data in form of List collection
     */
    private static void printPatientDetails(List<Patient> data) {
        for (Patient patient : data) {
            HumanName name = patient.getName().get(0);
            Date birthDate = patient.getBirthDate();
            // print first and last name of patient and print date of birth IF available in standard format
            System.out.printf("%-20s  %-20s %s \n", name.getGiven().get(0).toString(), name.getFamily(), (birthDate != null) ? new SimpleDateFormat("yyyy-MM-dd").format(birthDate) : "N/A");
        }
    }
}


/**
 * Comparator class to sort the patient details by First Name
 * To be used with List.sort() method
 * This is for Task 2 of basic tasks
 */
class SortByFirstName implements Comparator<Patient> {
    public int compare(Patient patient1, Patient patient2) {
        String name1 = patient1.getName().get(0).getGiven().get(0).toString();
        String name2 = patient2.getName().get(0).getGiven().get(0).toString();
        return name1.compareToIgnoreCase(name2);
    }
}