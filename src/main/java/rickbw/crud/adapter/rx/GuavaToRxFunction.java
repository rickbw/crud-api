package rickbw.crud.adapter.rx;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

import rx.util.functions.Func1;

public final class GuavaToRxFunction<FROM, TO> implements Func1<FROM, TO> {

    private final Function<? super FROM, ? extends TO> func;


    public GuavaToRxFunction(final Function<? super FROM, ? extends TO> func) {
        this.func = Preconditions.checkNotNull(func, "null function");
    }

    @Override
    @Nullable
    public TO call(@Nullable final FROM value) {
        @Nullable final TO transformedValue = this.func.apply(value);
        return transformedValue;
    }

}
