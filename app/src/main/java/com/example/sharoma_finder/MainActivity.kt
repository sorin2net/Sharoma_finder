package com.example.sharoma_finder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.sharoma_finder.screens.dashboard.DashboardScreen
import com.example.sharoma_finder.screens.results.ResultList
import com.example.sharoma_finder.ui.theme.Sharoma_FinderTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainApp()
        }
    }
}

sealed class Screen{
    data object Dashboard:Screen()
    data class Results(val id:String,val title:String):Screen()
}


@Composable
fun MainApp(){
    val systemUiController= rememberSystemUiController()
    systemUiController.setStatusBarColor(color= colorResource(R.color.white))

    val backStack=remember{ mutableStateListOf<Screen>(Screen.Dashboard) }
    val currentScreen=backStack.last()

    fun popBackStack(){
        if(backStack.size>1)
        {
            backStack.removeAt(backStack.lastIndex)
        }
    }
    BackHandler(enabled=backStack.size>1) {
        popBackStack()
    }
    when(val screen=currentScreen){
        Screen.Dashboard->{
            DashboardScreen(onCategoryClick = {id,title->
                backStack.add(Screen.Results(id,title))
            })
        }
        is Screen.Results->{
            ResultList(
                id=screen.id,
                title=screen.title,
                onBackClick = {
                    popBackStack()
                },
                onStoreClick = {
                    store->

                }

            )
        }
    }
}

