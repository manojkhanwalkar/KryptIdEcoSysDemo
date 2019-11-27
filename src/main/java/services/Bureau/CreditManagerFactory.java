package services.Bureau;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CreditManagerFactory {

      static class CreditManagerFactoryHolder {

        static CreditManagerFactory creditManagerFactory = new CreditManagerFactory();

    }

    public static CreditManagerFactory getInstance()
    {
        return CreditManagerFactoryHolder.creditManagerFactory;
    }

    private CreditManagerFactory()
    {

    }


    Map<Bureaus,CreditManager> bureaus = new HashMap<>();
    public enum Bureaus  { Experian, TransUnion, Equifax};

    public void init()
    {
        bureaus.put(Bureaus.Experian,new CreditManager(Bureaus.Experian));
        bureaus.put(Bureaus.TransUnion,new CreditManager(Bureaus.TransUnion));
        bureaus.put(Bureaus.Equifax,new CreditManager(Bureaus.Equifax));


    }

    public CreditManager get(Bureaus bureau)
    {
        return bureaus.get(bureau);
    }

    public List<CreditManager> get()
    {
        return bureaus.values().stream().collect(Collectors.toList());
    }

}
