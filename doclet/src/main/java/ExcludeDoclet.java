// Reference: https://sdgsystems.com/blog/hiding-javadoc-elements-exclude-tag

import com.sun.javadoc.Doc;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.javadoc.RootDoc;
import com.sun.tools.doclets.standard.Standard;
import com.sun.tools.javadoc.Main;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

public class ExcludeDoclet extends Standard {
    private static final String TAG_HIDE = "hide";

    public static void main(String[] args) {
        Main.execute(ExcludeDoclet.class.getName(), args);
    }

    public static boolean start(RootDoc root) {
        return Standard.start((RootDoc) process(root, RootDoc.class));
    }

    private static Object process(Object obj, Class<?> expect) {
        if (obj == null) {
            return null;
        }

        Class<?> clazz = obj.getClass();
        if (clazz.getName().startsWith("com.sun.")) {
            return Proxy.newProxyInstance(clazz.getClassLoader(),
                    clazz.getInterfaces(), new ExcludeHandler(obj));
        }

        if (obj instanceof Object[]) {
            Class<?> componentType = expect.getComponentType();
            Object[] array = (Object[]) obj;
            List<Object> list = new ArrayList<>(array.length);

            for (int i = 0; i < array.length; i++) {
                Object entry = array[i];
                if ((entry instanceof Doc) && exclude((Doc) entry)) {
                    continue;
                }

                list.add(process(entry, componentType));
            }

            return list.toArray((Object[]) Array.newInstance(componentType, list.size()));
        }

        return obj;
    }

    private static boolean exclude(Doc doc) {
        if (doc.tags(TAG_HIDE).length > 0) {
            return true;
        }
        if (doc instanceof ProgramElementDoc) {
            if (((ProgramElementDoc) doc).containingPackage().tags(TAG_HIDE).length > 0) {
                return true;
            }
        }

        return false;
    }

    private static class ExcludeHandler implements InvocationHandler {
        final Object target;

        ExcludeHandler(Object target) {
            this.target = target;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (args != null) {
                String methodName = method.getName();
                if (methodName.equals("compareTo")
                        || methodName.equals("equals")
                        || methodName.equals("overrides")
                        || methodName.equals("subclassOf")) {
                    args[0] = unwrap(args[0]);
                }
            }

            try {
                return process(method.invoke(target, args), method.getReturnType());
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }

        private Object unwrap(Object proxy) {
            if (proxy instanceof Proxy) {
                return ((ExcludeHandler) Proxy.getInvocationHandler(proxy)).target;
            }

            return proxy;
        }
    }
}
