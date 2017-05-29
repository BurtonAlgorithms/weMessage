package scott.wemessage.app.utils.view;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

public abstract class SingleFragmentActivity extends AppCompatActivity {

    private @LayoutRes int layoutId;
    private int containerId;

    public SingleFragmentActivity(@LayoutRes int layoutId, int containerId){
        this.layoutId = layoutId;
        this.containerId = containerId;
    }

    public abstract Fragment createFragment();

    @LayoutRes
    public int getLayoutResId(){
        return layoutId;
    }

    public int getContainerId(){
        return containerId;
    }

    @Override
    protected void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(getLayoutResId());

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(getContainerId());

        if (fragment == null){
            fragment = createFragment();
            fragmentManager.beginTransaction().add(getContainerId(), fragment).commit();
        }
    }
}
